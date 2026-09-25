package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.InstrumentPrice;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.queue.OrderQueue;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.InstrumentPriceRepository;
import com.agentsbackend.repos.HoldingsRepository;
import com.agentsbackend.repos.AccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for processing orders from the queue and fulfilling them if prices match.
 */
@Service
public class OrderFulfillmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderFulfillmentService.class);
    
    private final OrderQueue orderQueue;
    private final OrderRepository orderRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;
    private final HoldingsRepository holdingsRepository;
    private final AccountRepository accountRepository;
    private final AuditTrailService auditTrailService;
    
    public OrderFulfillmentService(OrderQueue orderQueue, OrderRepository orderRepository, 
                                  InstrumentPriceRepository instrumentPriceRepository,
                                  HoldingsRepository holdingsRepository,
                                  AccountRepository accountRepository,
                                  AuditTrailService auditTrailService) {
        this.orderQueue = orderQueue;
        this.orderRepository = orderRepository;
        this.instrumentPriceRepository = instrumentPriceRepository;
        this.holdingsRepository = holdingsRepository;
        this.accountRepository = accountRepository;
        this.auditTrailService = auditTrailService;
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
     * Market orders (limit_price = NULL): fill immediately at current market price
     * Limit orders: fill only if current price matches limit price
     * Returns true if order was filled, false otherwise.
     */
    private boolean tryFillOrder(Order order) {
        // Get current market price for the instrument
        InstrumentPrice currentPrice = instrumentPriceRepository.findLatestPrice(
            order.getInstrument().getInstrumentId()
        );
        
        if (currentPrice == null) {
            logger.warn("No market price available for instrument {}", 
                order.getInstrument().getInstrumentId());
            return false;
        }
        
        BigDecimal marketPrice = currentPrice.getPrice();
        
        // Market order: fill immediately at current price
        if (order.getLimitPrice() == null) {
            fillOrder(order, marketPrice);
            return true;
        }
        
        // Limit order: check if price matches
        BigDecimal orderPrice = order.getLimitPrice();
        boolean canFill = false;
        
        if (order.getOrderType().toString().equals("BUY")) {
            // BUY orders: fill if market price <= order price
            canFill = marketPrice.compareTo(orderPrice) <= 0;
        } else if (order.getOrderType().toString().equals("SELL")) {
            // SELL orders: fill if market price >= order price
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
        order.setFilledAt(LocalDateTime.now());
        
        // Update order in database
        orderRepository.updateOrder(order);
        
        // Update holdings and account based on order type
        if (order.getOrderType().equals(OrderType.BUY)) {
            updateHoldingsForBuy(order, filledPrice);
            updateCashForBuy(order, filledPrice);
        } else if (order.getOrderType().equals(OrderType.SELL)) {
            updateHoldingsForSell(order);
            updateCashForSell(order, filledPrice);
        }
        
        // Log order fill to audit trail
        Account fullAccount = accountRepository.findById(order.getAccount().getAccountId());
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
     * Update holdings for a BUY order - increase quantity
     */
    private void updateHoldingsForBuy(Order order, BigDecimal filledPrice) {
        Holding holding = holdingsRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding == null) {
            // Create new holding
            holding = new Holding();
            holding.setHoldingId(UUID.randomUUID());
            
            // Ensure Account and Instrument objects have their IDs properly set
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
            
            holding.setQuantity(order.getQuantity());
            // Set average cost basis for new holding
            holding.setAverageCostBasis(filledPrice);
        } else {
            // Update existing holding - recalculate average cost basis
            BigDecimal currentValue = holding.getQuantity().multiply(holding.getAverageCostBasis());
            BigDecimal newSharesValue = order.getQuantity().multiply(filledPrice);
            BigDecimal totalValue = currentValue.add(newSharesValue);
            BigDecimal totalQuantity = holding.getQuantity().add(order.getQuantity());
            
            holding.setQuantity(totalQuantity);
            holding.setAverageCostBasis(totalValue.divide(totalQuantity, 4, java.math.RoundingMode.HALF_UP));
            
            // Ensure Account and Instrument IDs are properly set before saving
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
        }
        
        holdingsRepository.save(holding);
    }
    
    /**
     * Update holdings for a SELL order - decrease quantity
     */
    private void updateHoldingsForSell(Order order) {
        Holding holding = holdingsRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding != null) {
            BigDecimal newQuantity = holding.getQuantity().subtract(order.getQuantity());
            
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // Delete holding if quantity becomes zero
                holdingsRepository.deleteByAccountAndInstrument(
                    order.getAccount().getAccountId(),
                    order.getInstrument().getInstrumentId()
                );
                logger.debug("Holding deleted for account {} instrument {} (quantity 0)", 
                    order.getAccount().getAccountId(), order.getInstrument().getInstrumentId());
            } else {
                // Update quantity - ensure Account and Instrument IDs are properly set
                holding.setQuantity(newQuantity);
                
                Account account = new Account();
                account.setAccountId(order.getAccount().getAccountId());
                holding.setAccount(account);
                
                Instrument instrument = new Instrument();
                instrument.setInstrumentId(order.getInstrument().getInstrumentId());
                holding.setInstrument(instrument);
                
                holdingsRepository.save(holding);
            }
        }
    }
    
    /**
     * Update account cash balance for BUY order - decrease cash
     */
    private void updateCashForBuy(Order order, BigDecimal filledPrice) {
        Account account = accountRepository.findById(order.getAccount().getAccountId());
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
        Account account = accountRepository.findById(order.getAccount().getAccountId());
        if (account != null) {
            BigDecimal orderProceeds = filledPrice.multiply(order.getQuantity());
            account.setCashBalance(account.getCashBalance().add(orderProceeds));
            accountRepository.save(account);
            logger.debug("Updated cash balance for SELL order {}: +${}", order.getOrderId(), orderProceeds);
        }
    }
}
