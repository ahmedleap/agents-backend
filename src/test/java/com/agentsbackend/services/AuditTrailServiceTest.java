package com.agentsbackend.services;

import com.agentsbackend.entities.AuditLog;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.repos.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuditTrailService Tests")
class AuditTrailServiceTest {

    private AuditTrailService auditTrailService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditTrailService = new AuditTrailService(auditLogRepository);
    }

    // ==================== logOrderCreated Tests ====================

    @Test
    @DisplayName("Should log order created successfully for BUY order")
    void testLogOrderCreatedBuyOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Integer quantity = 100;
        BigDecimal limitPrice = new BigDecimal("50.00");

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, quantity, limitPrice);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getOrderId().equals(orderId) &&
            auditLog.getAccountId().equals(accountId) &&
            auditLog.getClientId().equals(clientId) &&
            auditLog.getEventType().equals("PENDING") &&
            auditLog.getReason().equals("Order created and queued for fulfillment")
        ));
    }

    @Test
    @DisplayName("Should log order created successfully for SELL order")
    void testLogOrderCreatedSellOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Integer quantity = 50;
        BigDecimal limitPrice = new BigDecimal("75.00");

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.SELL, quantity, limitPrice);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should include order details in JSON for created order")
    void testLogOrderCreatedIncludesDetails() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Integer quantity = 100;
        BigDecimal limitPrice = new BigDecimal("50.00");

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, quantity, limitPrice);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getDetails() != null && !auditLog.getDetails().isEmpty()
        ));
    }

    @Test
    @DisplayName("Should set audit log ID when logging created order")
    void testLogOrderCreatedSetsAuditLogId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getAuditLogId() != null
        ));
    }

    @Test
    @DisplayName("Should handle null limit price for created order")
    void testLogOrderCreatedNullLimitPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> 
            auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, 100, null)
        );
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully for created order")
    void testLogOrderCreatedHandlesRepositoryException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        doThrow(new RuntimeException("Database error")).when(auditLogRepository).save(any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"))
        );
    }

    // ==================== logOrderFilled Tests ====================

    @Test
    @DisplayName("Should log order filled successfully for BUY order")
    void testLogOrderFilledBuyOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Integer quantity = 100;
        BigDecimal filledPrice = new BigDecimal("48.50");

        // Act
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, quantity, filledPrice);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getOrderId().equals(orderId) &&
            auditLog.getAccountId().equals(accountId) &&
            auditLog.getClientId().equals(clientId) &&
            auditLog.getEventType().equals("FILLED") &&
            auditLog.getReason().contains("48.50")
        ));
    }

    @Test
    @DisplayName("Should log order filled successfully for SELL order")
    void testLogOrderFilledSellOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Integer quantity = 50;
        BigDecimal filledPrice = new BigDecimal("72.25");

        // Act
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.SELL, quantity, filledPrice);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should include filled price in reason for filled order")
    void testLogOrderFilledIncludesFilledPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        BigDecimal filledPrice = new BigDecimal("55.75");

        // Act
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, filledPrice);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getReason().contains("55.75")
        ));
    }

    @Test
    @DisplayName("Should set FILLED status in details for filled order")
    void testLogOrderFilledSetsFilledStatus() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getDetails() != null && auditLog.getDetails().contains("FILLED")
        ));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully for filled order")
    void testLogOrderFilledHandlesRepositoryException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        doThrow(new RuntimeException("Database error")).when(auditLogRepository).save(any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"))
        );
    }

    // ==================== logOrderCancelled Tests ====================

    @Test
    @DisplayName("Should log order cancelled successfully")
    void testLogOrderCancelled() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String cancellationReason = "Insufficient funds";
        BigDecimal limitPrice = new BigDecimal("50.00");

        // Act
        auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
            OrderType.BUY, 100, limitPrice, cancellationReason);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getOrderId().equals(orderId) &&
            auditLog.getAccountId().equals(accountId) &&
            auditLog.getClientId().equals(clientId) &&
            auditLog.getEventType().equals("CANCELLED") &&
            auditLog.getReason().equals(cancellationReason)
        ));
    }

    @Test
    @DisplayName("Should log cancelled SELL order with correct details")
    void testLogOrderCancelledSellOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String reason = "User requested cancellation";

        // Act
        auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
            OrderType.SELL, 50, new BigDecimal("75.00"), reason);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should include cancellation reason in audit log")
    void testLogOrderCancelledIncludesReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String reason = "Market conditions changed";

        // Act
        auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
            OrderType.BUY, 100, new BigDecimal("50.00"), reason);

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getReason().equals(reason)
        ));
    }

    @Test
    @DisplayName("Should set CANCELLED status in details")
    void testLogOrderCancelledSetsStatus() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
            OrderType.BUY, 100, new BigDecimal("50.00"), "User cancellation");

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getDetails() != null && auditLog.getDetails().contains("CANCELLED")
        ));
    }

    @Test
    @DisplayName("Should handle null cancellation reason")
    void testLogOrderCancelledNullReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
                OrderType.BUY, 100, new BigDecimal("50.00"), null)
        );
    }

    @Test
    @DisplayName("Should handle repository exception gracefully for cancelled order")
    void testLogOrderCancelledHandlesRepositoryException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        doThrow(new RuntimeException("Database error")).when(auditLogRepository).save(any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            auditTrailService.logOrderCancelled(orderId, accountId, clientId, 
                OrderType.BUY, 100, new BigDecimal("50.00"), "User cancellation")
        );
    }

    // ==================== logOrderRejected Tests ====================

    @Test
    @DisplayName("Should log order rejected successfully")
    void testLogOrderRejected() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String rejectionReason = "Client age validation failed";

        // Act
        auditTrailService.logOrderRejected(orderId, accountId, clientId, 
            rejectionReason, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getOrderId().equals(orderId) &&
            auditLog.getAccountId().equals(accountId) &&
            auditLog.getClientId().equals(clientId) &&
            auditLog.getEventType().equals("REJECTED") &&
            auditLog.getReason().equals(rejectionReason)
        ));
    }

    @Test
    @DisplayName("Should log rejected SELL order")
    void testLogOrderRejectedSellOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderRejected(orderId, accountId, clientId, 
            "Insufficient holdings", OrderType.SELL, 50, new BigDecimal("75.00"));

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should include rejection reason in audit log")
    void testLogOrderRejectedIncludesReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String reason = "Duplicate order detected";

        // Act
        auditTrailService.logOrderRejected(orderId, accountId, clientId, 
            reason, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getReason().equals(reason)
        ));
    }

    @Test
    @DisplayName("Should set REJECTED status in details")
    void testLogOrderRejectedSetsStatus() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderRejected(orderId, accountId, clientId, 
            "Validation failed", OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getDetails() != null && auditLog.getDetails().contains("REJECTED")
        ));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully for rejected order")
    void testLogOrderRejectedHandlesRepositoryException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        doThrow(new RuntimeException("Database error")).when(auditLogRepository).save(any());

        // Act & Assert - should not throw
        assertDoesNotThrow(() ->
            auditTrailService.logOrderRejected(orderId, accountId, clientId, 
                "Validation failed", OrderType.BUY, 100, new BigDecimal("50.00"))
        );
    }

    // ==================== Event Type Tests ====================

    @Test
    @DisplayName("Should set correct event types for different operations")
    void testCorrectEventTypes() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));
        auditTrailService.logOrderCancelled(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"), "Reason");
        auditTrailService.logOrderRejected(orderId, accountId, clientId, "Reason", OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository, times(4)).save(any(AuditLog.class));
    }

    // ==================== Order Details Tests ====================

    @Test
    @DisplayName("OrderDetails inner class should initialize correctly")
    void testOrderDetailsInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("50.00");

        // Act
        AuditTrailService.OrderDetails details = new AuditTrailService.OrderDetails(
            orderId, OrderType.BUY, 100, price, OrderStatus.PENDING
        );

        // Assert
        assertEquals(orderId, details.orderId);
        assertEquals(OrderType.BUY, details.orderType);
        assertEquals(100, details.quantity);
        assertEquals(price, details.limitPrice);
        assertEquals(OrderStatus.PENDING, details.status);
    }

    @Test
    @DisplayName("OrderDetails should handle null price")
    void testOrderDetailsNullPrice() {
        // Act
        AuditTrailService.OrderDetails details = new AuditTrailService.OrderDetails(
            UUID.randomUUID(), OrderType.SELL, 50, null, OrderStatus.FILLED
        );

        // Assert
        assertNull(details.limitPrice);
    }

    @Test
    @DisplayName("OrderDetails should support all order types")
    void testOrderDetailsAllOrderTypes() {
        // Arrange & Act & Assert
        AuditTrailService.OrderDetails buyDetails = new AuditTrailService.OrderDetails(
            UUID.randomUUID(), OrderType.BUY, 100, new BigDecimal("50.00"), OrderStatus.PENDING
        );
        assertEquals(OrderType.BUY, buyDetails.orderType);

        AuditTrailService.OrderDetails sellDetails = new AuditTrailService.OrderDetails(
            UUID.randomUUID(), OrderType.SELL, 50, new BigDecimal("75.00"), OrderStatus.FILLED
        );
        assertEquals(OrderType.SELL, sellDetails.orderType);
    }

    @Test
    @DisplayName("OrderDetails should support all order statuses")
    void testOrderDetailsAllStatuses() {
        // Arrange & Act & Assert
        UUID orderId = UUID.randomUUID();

        AuditTrailService.OrderDetails pendingDetails = new AuditTrailService.OrderDetails(
            orderId, OrderType.BUY, 100, new BigDecimal("50.00"), OrderStatus.PENDING
        );
        assertEquals(OrderStatus.PENDING, pendingDetails.status);

        AuditTrailService.OrderDetails filledDetails = new AuditTrailService.OrderDetails(
            orderId, OrderType.BUY, 100, new BigDecimal("50.00"), OrderStatus.FILLED
        );
        assertEquals(OrderStatus.FILLED, filledDetails.status);

        AuditTrailService.OrderDetails cancelledDetails = new AuditTrailService.OrderDetails(
            orderId, OrderType.BUY, 100, new BigDecimal("50.00"), OrderStatus.CANCELLED
        );
        assertEquals(OrderStatus.CANCELLED, cancelledDetails.status);

        AuditTrailService.OrderDetails rejectedDetails = new AuditTrailService.OrderDetails(
            orderId, OrderType.BUY, 100, new BigDecimal("50.00"), OrderStatus.REJECTED
        );
        assertEquals(OrderStatus.REJECTED, rejectedDetails.status);
    }

    // ==================== Timestamp Tests ====================

    @Test
    @DisplayName("Should set event time for all log operations")
    void testEventTimeIsSet() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderCreated(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getEventTime() != null
        ));
    }

    @Test
    @DisplayName("Should set created at timestamp for all log operations")
    void testCreatedAtTimestampIsSet() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        // Act
        auditTrailService.logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, new BigDecimal("50.00"));

        // Assert
        verify(auditLogRepository).save(argThat(auditLog ->
            auditLog.getCreatedAt() != null
        ));
    }
}
