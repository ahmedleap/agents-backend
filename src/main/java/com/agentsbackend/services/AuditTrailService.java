package com.agentsbackend.services;

import com.agentsbackend.entities.AuditLog;
import com.agentsbackend.repos.AuditLogRepository;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.OrderStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuditTrailService {

    private static final Logger logger = LoggerFactory.getLogger(AuditTrailService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditTrailService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Log a rejected order event to the audit trail
     */
    public void logOrderRejected(UUID orderId, UUID accountId, UUID clientId, 
                                 String rejectionReason, OrderType orderType, 
                                 Integer quantity, java.math.BigDecimal limitPrice) {
        try {
            logger.info("Logging rejected order {} for account {}, client {}", orderId, accountId, clientId);
            
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditLogId(UUID.randomUUID());
            auditLog.setOrderId(orderId);
            auditLog.setAccountId(accountId);
            auditLog.setClientId(clientId);
            auditLog.setEventType("REJECTED");
            auditLog.setEventTime(LocalDateTime.now());
            auditLog.setReason(rejectionReason);
            
            // Create order details snapshot as JSON
            OrderDetails details = new OrderDetails(orderId, orderType, quantity, limitPrice, OrderStatus.REJECTED);
            auditLog.setDetails(objectMapper.writeValueAsString(details));
            
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            logger.info("Successfully logged rejected order {} for account {}", orderId, accountId);
        } catch (Exception e) {
            logger.error("Error logging rejected order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Log a created order event (PENDING status) to the audit trail
     */
    public void logOrderCreated(UUID orderId, UUID accountId, UUID clientId,
                                OrderType orderType, Integer quantity, java.math.BigDecimal limitPrice) {
        try {
            logger.info("Logging created order {} for account {}, client {}", orderId, accountId, clientId);
            
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditLogId(UUID.randomUUID());
            auditLog.setOrderId(orderId);
            auditLog.setAccountId(accountId);
            auditLog.setClientId(clientId);
            auditLog.setEventType("PENDING");
            auditLog.setEventTime(LocalDateTime.now());
            auditLog.setReason("Order created and queued for fulfillment");
            
            // Create order details snapshot as JSON
            OrderDetails details = new OrderDetails(orderId, orderType, quantity, limitPrice, OrderStatus.PENDING);
            auditLog.setDetails(objectMapper.writeValueAsString(details));
            
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            logger.info("Successfully logged created order {} for account {}", orderId, accountId);
        } catch (Exception e) {
            logger.error("Error logging created order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Log a filled order event to the audit trail
     */
    public void logOrderFilled(UUID orderId, UUID accountId, UUID clientId,
                               OrderType orderType, Integer quantity, java.math.BigDecimal filledPrice) {
        try {
            logger.info("Logging filled order {} for account {}, client {}", orderId, accountId, clientId);
            
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditLogId(UUID.randomUUID());
            auditLog.setOrderId(orderId);
            auditLog.setAccountId(accountId);
            auditLog.setClientId(clientId);
            auditLog.setEventType("FILLED");
            auditLog.setEventTime(LocalDateTime.now());
            auditLog.setReason("Order filled at price: " + filledPrice);
            
            // Create order details snapshot as JSON with filled price
            OrderDetails details = new OrderDetails(orderId, orderType, quantity, filledPrice, OrderStatus.FILLED);
            auditLog.setDetails(objectMapper.writeValueAsString(details));
            
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            logger.info("Successfully logged filled order {} for account {}", orderId, accountId);
        } catch (Exception e) {
            logger.error("Error logging filled order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Log a cancelled order event to the audit trail
     */
    public void logOrderCancelled(UUID orderId, UUID accountId, UUID clientId,
                                  OrderType orderType, Integer quantity, java.math.BigDecimal limitPrice,
                                  String cancellationReason) {
        try {
            logger.info("Logging cancelled order {} for account {}, client {}", orderId, accountId, clientId);
            
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditLogId(UUID.randomUUID());
            auditLog.setOrderId(orderId);
            auditLog.setAccountId(accountId);
            auditLog.setClientId(clientId);
            auditLog.setEventType("CANCELLED");
            auditLog.setEventTime(LocalDateTime.now());
            auditLog.setReason(cancellationReason);
            
            // Create order details snapshot as JSON
            OrderDetails details = new OrderDetails(orderId, orderType, quantity, limitPrice, OrderStatus.CANCELLED);
            auditLog.setDetails(objectMapper.writeValueAsString(details));
            
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            logger.info("Successfully logged cancelled order {} for account {}", orderId, accountId);
        } catch (Exception e) {
            logger.error("Error logging cancelled order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Inner class for JSON serialization of order details
     */
    public static class OrderDetails {
        public UUID orderId;
        public OrderType orderType;
        public Integer quantity;
        public java.math.BigDecimal limitPrice;
        public OrderStatus status;

        public OrderDetails(UUID orderId, OrderType orderType, Integer quantity, 
                           java.math.BigDecimal limitPrice, OrderStatus status) {
            this.orderId = orderId;
            this.orderType = orderType;
            this.quantity = quantity;
            this.limitPrice = limitPrice;
            this.status = status;
        }
    }
}
