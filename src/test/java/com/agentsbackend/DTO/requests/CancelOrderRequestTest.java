package com.agentsbackend.DTO.requests;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancelOrderRequest DTO Tests")
class CancelOrderRequestTest {

    @Test
    @DisplayName("Should create CancelOrderRequest with order ID")
    void testCancelOrderRequestInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);

        // Assert
        assertEquals(orderId, request.getOrderId());
    }

    @Test
    @DisplayName("Should update order ID")
    void testCancelOrderRequestUpdateId() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        // Act
        request.setOrderId(orderId1);
        assertEquals(orderId1, request.getOrderId());
        
        request.setOrderId(orderId2);

        // Assert
        assertEquals(orderId2, request.getOrderId());
    }

    @Test
    @DisplayName("Should handle null order ID")
    void testCancelOrderRequestNullId() {
        // Arrange & Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(null);

        // Assert
        assertNull(request.getOrderId());
    }
}
