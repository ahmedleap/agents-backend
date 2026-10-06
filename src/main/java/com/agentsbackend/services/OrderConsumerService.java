package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Consumer Service for processing orders asynchronously.
 * Listens to the orders topic and fulfills orders if price conditions are met.
 * Active only when order.processing.mode=kafka
 */
@Service
@ConditionalOnProperty(
    name = "order.processing.mode",
    havingValue = "kafka"
)
public class OrderConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final AuditTrailService auditTrailService;
    private final HoldingService holdingsService;
    
    public OrderConsumerService(OrderRepository orderRepository,
                               AccountRepository accountRepository,
                               InstrumentRepository instrumentRepository,
                               AuditTrailService auditTrailService,
                               HoldingService holdingsService) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.auditTrailService = auditTrailService;
        this.holdingsService = holdingsService;
    }
    
    /**
     * Consume orders from Kafka topic and attempt to fill them.
     */
    @KafkaListener(topics = "order-fulfillment", groupId = "order-fulfillment-group")
    public void processOrder(Order order) {
        try {
            logger.info("Received order {} from Kafka", order.getOrderId());
            if (tryFillOrder(order)) {
                logger.info("Order {} filled successfully", order.getOrderId());
            } else {
                logger.info("Order {} could not be filled at current market price", order.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error processing order {}: {}", order.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * Attempt to fill an order if conditions are met.
     * Market orders (limit_price = NULL): fill immediately at current bid/ask (depending on order type)
     * Limit orders: fill based on bid/ask comparison with limit price
     * 
     * BUY orders: checked against ASK price (what sellers are asking)
     * SELL orders: checked against BID price (what buyers are bidding)
     * 
     * Returns true if order was filled, false otherwise.
     */
    private boolean tryFillOrder(Order order) {
        // Get current instrument pricing (bid/ask/mid_price)
        Instrument instrument = instrumentRepository.findById(
            order.getInstrument().getInstrumentId()
        ).orElse(null);
        
        if (instrument == null) {
            logger.warn("Instrument {} not found for order {}", 
                order.getInstrument().getInstrumentId(), order.getOrderId());
            return false;
        }
        BigDecimal bid = instrument.getBid();
        BigDecimal ask = instrument.getAsk();
        
        // Determine relevant market price based on order type
        BigDecimal marketPrice;
        if (order.getOrderType().equals(OrderType.BUY)) {
            // For BUY orders, use ASK price (what we pay to sellers)
            marketPrice = ask;
        } else {
            // For SELL orders, use BID price (what we receive from buyers)
            marketPrice = bid;
        }
        
        if (marketPrice == null) {
            logger.warn("No market price (bid/ask) available for instrument {} for {} order", 
                instrument.getInstrumentId(), order.getOrderType());
            return false;
        }
        
        // Market order: fill immediately at current market price
        if (order.getLimitPrice() == null) {
            fillOrder(order, marketPrice);
            return true;
        }
        
        // Limit order: check if market price allows execution
        BigDecimal orderPrice = order.getLimitPrice();
        boolean canFill = false;
        
        if (order.getOrderType().equals(OrderType.BUY)) {
            // BUY orders: fill if ASK price <= order price (we get a good deal or equal)
            canFill = marketPrice.compareTo(orderPrice) <= 0;
            logger.debug("BUY order {}: ASK={} vs limit={}, canFill={}", 
                order.getOrderId(), marketPrice, orderPrice, canFill);
        } else if (order.getOrderType().equals(OrderType.SELL)) {
            // SELL orders: fill if BID price >= order price (we get a good deal or equal)
            canFill = marketPrice.compareTo(orderPrice) >= 0;
            logger.debug("SELL order {}: BID={} vs limit={}, canFill={}", 
                order.getOrderId(), marketPrice, orderPrice, canFill);
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
        order.setFilledAt(OffsetDateTime.now(ZoneOffset.UTC));
        
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
