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
    @DisplayName("Should create CancelOrderResponse with all fields")
    void testCancelOrderResponseInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime cancelledAt = LocalDateTime.now();

        // Act
        CancelOrderResponse response = new CancelOrderResponse(
            orderId, OrderStatus.CANCELLED, cancelledAt, "Order cancelled successfully"
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(cancelledAt, response.getCancelledAt());
        assertEquals("Order cancelled successfully", response.getMessage());
    }

    @Test
    @DisplayName("Should create empty CancelOrderResponse")
    void testCancelOrderResponseEmpty() {
        // Act
        CancelOrderResponse response = new CancelOrderResponse();

        // Assert
        assertNull(response.getOrderId());
        assertNull(response.getStatus());
        assertNull(response.getCancelledAt());
        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("Should set and get order ID")
    void testCancelOrderResponseOrderId() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();
        UUID orderId = UUID.randomUUID();

        // Act
        response.setOrderId(orderId);

        // Assert
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("Should set and get status")
    void testCancelOrderResponseStatus() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();

        // Act
        response.setStatus(OrderStatus.CANCELLED);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
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

    @Test
    @DisplayName("Should set and get message")
    void testCancelOrderResponseMessage() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();
        String message = "Order cancelled successfully by user request";

        // Act
        response.setMessage(message);

        // Assert
        assertEquals(message, response.getMessage());
    }

    @Test
    @DisplayName("Should handle null message")
    void testCancelOrderResponseNullMessage() {
        // Arrange & Act
        CancelOrderResponse response = new CancelOrderResponse();
        response.setMessage(null);

        // Assert
        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("Should update all fields independently")
    void testCancelOrderResponseUpdateFields() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();
        UUID orderId = UUID.randomUUID();
        LocalDateTime cancelledAt = LocalDateTime.now();

        // Act
        response.setOrderId(orderId);
        response.setStatus(OrderStatus.CANCELLED);
        response.setCancelledAt(cancelledAt);
        response.setMessage("Cancelled");

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(cancelledAt, response.getCancelledAt());
        assertEquals("Cancelled", response.getMessage());
    }

    @Test
    @DisplayName("Should preserve cancelled timestamp")
    void testCancelOrderResponsePreserveTimestamp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime cancelledAt = LocalDateTime.now().minusMinutes(5);

        // Act
        CancelOrderResponse response = new CancelOrderResponse(
            orderId, OrderStatus.CANCELLED, cancelledAt, "Order cancelled"
        );

        // Assert
        assertEquals(cancelledAt, response.getCancelledAt());
    }

    @Test
    @DisplayName("Should handle different cancelled timestamps")
    void testCancelOrderResponseDifferentTimestamps() {
        // Arrange
        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = LocalDateTime.now().minusHours(1);

        // Act
        CancelOrderResponse response1 = new CancelOrderResponse();
        CancelOrderResponse response2 = new CancelOrderResponse();

        response1.setCancelledAt(time1);
        response2.setCancelledAt(time2);

        // Assert
        assertEquals(time1, response1.getCancelledAt());
        assertEquals(time2, response2.getCancelledAt());
        assertNotEquals(response1.getCancelledAt(), response2.getCancelledAt());
    }

    @Test
    @DisplayName("Should create response for cancelled BUY order")
    void testCancelOrderResponseBuyOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime cancelledAt = LocalDateTime.now();

        // Act
        CancelOrderResponse response = new CancelOrderResponse(
            orderId, OrderStatus.CANCELLED, cancelledAt, "Buy order cancelled"
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Should create response for cancelled SELL order")
    void testCancelOrderResponseSellOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime cancelledAt = LocalDateTime.now();

        // Act
        CancelOrderResponse response = new CancelOrderResponse(
            orderId, OrderStatus.CANCELLED, cancelledAt, "Sell order cancelled"
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Should handle rapid timestamp updates")
    void testCancelOrderResponseRapidUpdates() {
        // Arrange
        CancelOrderResponse response = new CancelOrderResponse();
        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = time1.plusSeconds(1);
        LocalDateTime time3 = time2.plusSeconds(1);

        // Act
        response.setCancelledAt(time1);
        assertEquals(time1, response.getCancelledAt());

        response.setCancelledAt(time2);
        assertEquals(time2, response.getCancelledAt());

        response.setCancelledAt(time3);

        // Assert
        assertEquals(time3, response.getCancelledAt());
        assertNotEquals(time1, response.getCancelledAt());
    }

    @Test
    @DisplayName("Should support constructor and setter combination")
    void testCancelOrderResponseConstructorAndSetter() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        // Act
        CancelOrderResponse response = new CancelOrderResponse(
            orderId1, OrderStatus.CANCELLED, LocalDateTime.now(), "Initial"
        );
        response.setOrderId(orderId2);
        response.setMessage("Updated");

        // Assert
        assertEquals(orderId2, response.getOrderId());
        assertEquals("Updated", response.getMessage());
    }
}
