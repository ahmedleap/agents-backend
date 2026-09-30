package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.queue.OrderQueue;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for processing orders from the queue and fulfilling them if prices match.
 * Uses instrument mid_price from the instruments table for market pricing.
 */
@Service
public class OrderFulfillmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderFulfillmentService.class);
    
    private final OrderQueue orderQueue;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final AuditTrailService auditTrailService;
    private final HoldingsService holdingsService;
    
    public OrderFulfillmentService(OrderQueue orderQueue, OrderRepository orderRepository, 
                                  AccountRepository accountRepository,
                                  InstrumentRepository instrumentRepository,
                                  AuditTrailService auditTrailService,
                                  HoldingsService holdingsService) {
        this.orderQueue = orderQueue;
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.auditTrailService = auditTrailService;
        this.holdingsService = holdingsService;
    }
    
    /**
     * Process pending orders from the queue every 5 seconds.
     * Checks if current market price matches order prices and fills them.
     */
    @Scheduled(fixedDelay = 5000) // 5 seconds
    public void processPendingOrders() {
        List<Order> pendingOrders = orderQueue.getPendingOrders();
        
        if (pendingOrders.isEmpty()) {
            return;
        }
        
        logger.debug("Processing {} pending orders from queue", pendingOrders.size());
        
        for (Order order : pendingOrders) {
            try {
                if (tryFillOrder(order)) {
                    orderQueue.remove(order);
                    logger.info("Order {} filled and removed from queue", order.getOrderId());
                }
            } catch (Exception e) {
                logger.error("Error processing order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
    }
    
    /**
     * Attempt to fill an order if conditions are met.
     * Market orders (limit_price = NULL): fill immediately at current mid_price
     * Limit orders: fill only if current mid_price matches limit price
     * Returns true if order was filled, false otherwise.
     */
    private boolean tryFillOrder(Order order) {
        // Get current instrument pricing (bid/ask/mid_price)
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(
            order.getInstrument().getInstrumentId()
        );
        
        if (!instrumentOpt.isPresent()) {
            logger.warn("Instrument {} not found for order {}", 
                order.getInstrument().getInstrumentId(), order.getOrderId());
            return false;
        }
        
        Instrument instrument = instrumentOpt.get();
        BigDecimal marketPrice = instrument.getMidPrice();
        
        if (marketPrice == null) {
            logger.warn("No market price (mid_price) available for instrument {}", 
                instrument.getInstrumentId());
            return false;
        }
        
        // Market order: fill immediately at current price
        if (order.getLimitPrice() == null) {
            fillOrder(order, marketPrice);
            return true;
        }
        
        // Limit order: check if price matches
        BigDecimal orderPrice = order.getLimitPrice();
        boolean canFill = false;
        
        if (order.getOrderType().equals(OrderType.BUY)) {
            // BUY orders: fill if market price <= order price (buyer's advantage)
            canFill = marketPrice.compareTo(orderPrice) <= 0;
        } else if (order.getOrderType().equals(OrderType.SELL)) {
            // SELL orders: fill if market price >= order price (seller's advantage)
            canFill = marketPrice.compareTo(orderPrice) >= 0;
        }
        
        if (canFill) {
            fillOrder(order, marketPrice);
            return true;
        }
        
        return false;
    }
    
    /**
     * Fill an order with the given filled price.
     * For BUY orders: increase holdings and decrease cash balance
     * For SELL orders: decrease holdings and increase cash balance
     */
    private void fillOrder(Order order, BigDecimal filledPrice) {
        order.setStatus(OrderStatus.FILLED);
        order.setFilledPrice(filledPrice);
        order.setFilledAt(LocalDateTime.now(ZoneId.of("UTC")));
        
        // Update order in database
        orderRepository.updateOrder(order);
        
        // Update holdings and account based on order type
        if (order.getOrderType().equals(OrderType.BUY)) {
            holdingsService.updateHoldingsForBuy(order, filledPrice);
            updateCashForBuy(order, filledPrice);
        } else if (order.getOrderType().equals(OrderType.SELL)) {
            holdingsService.updateHoldingsForSell(order);
            updateCashForSell(order, filledPrice);
        }
        
        // Log order fill to audit trail
        Account fullAccount = accountRepository.findById(order.getAccount().getAccountId()).orElse(null);
        if (fullAccount != null && fullAccount.getClientId() != null) {
            auditTrailService.logOrderFilled(
                order.getOrderId(),
                order.getAccount().getAccountId(),
                fullAccount.getClientId(),
                order.getOrderType(),
                order.getQuantity().intValue(),
                filledPrice
            );
        } else {
            logger.warn("Could not log order fill for order {}: Account or clientId not found", order.getOrderId());
        }
        
        logger.info("Order {} filled at price {}", order.getOrderId(), filledPrice);
    }
    
    
    /**
     * Update account cash balance for BUY order - decrease cash
     */
    private void updateCashForBuy(Order order, BigDecimal filledPrice) {
        Account account = accountRepository.findById(order.getAccount().getAccountId()).orElse(null);
        if (account != null) {
            BigDecimal orderCost = filledPrice.multiply(order.getQuantity());
            account.setCashBalance(account.getCashBalance().subtract(orderCost));
            accountRepository.save(account);
            logger.debug("Updated cash balance for BUY order {}: -${}", order.getOrderId(), orderCost);
        }
    }
    
    /**
     * Update account cash balance for SELL order - increase cash
     */
    private void updateCashForSell(Order order, BigDecimal filledPrice) {
        Account account = accountRepository.findById(order.getAccount().getAccountId()).orElse(null);
        if (account != null) {
            BigDecimal orderProceeds = filledPrice.multiply(order.getQuantity());
            account.setCashBalance(account.getCashBalance().add(orderProceeds));
            accountRepository.save(account);
            logger.debug("Updated cash balance for SELL order {}: +${}", order.getOrderId(), orderProceeds);
        }
    }
}
