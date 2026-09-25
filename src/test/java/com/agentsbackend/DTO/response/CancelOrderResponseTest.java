package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancelOrderResponse DTO Tests")
class CancelOrderResponseTest {

    @Test
    @DisplayName("Should create CancelOrderResponse with order ID")
    void testCancelOrderResponseInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CancelOrderResponse response = new CancelOrderResponse();
        response.setOrderId(orderId);
        response.setStatus(OrderStatus.CANCELLED);
        response.setMessage("Order cancelled");

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals("Order cancelled", response.getMessage());
    }

    @Test
    @DisplayName("Should set cancelled at timestamp")
    void testCancelOrderResponseCancelledAt() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();
        LocalDateTime cancelledTime = LocalDateTime.now();

        // Act
        response.setCancelledAt(cancelledTime);

        // Assert
        assertEquals(cancelledTime, response.getCancelledAt());
    }

    @Test
    @DisplayName("Should handle null cancelled at")
    void testCancelOrderResponseNullCancelledAt() {
        // Arrange & Act
        CancelOrderResponse response = new CancelOrderResponse();
        response.setCancelledAt(null);

        // Assert
        assertNull(response.getCancelledAt());
    }

    @Test
    @DisplayName("Should update order status")
    void testCancelOrderResponseUpdateStatus() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();

        // Act
        response.setStatus(OrderStatus.CANCELLED);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }
}
