package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOrderResponse DTO Tests")
class CreateOrderResponseTest {

    @Test
    @DisplayName("Should create CreateOrderResponse with all fields")
    void testCreateOrderResponseInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(orderId);
        response.setAccountId(accountId);
        response.setInstrumentId(instrumentId);
        response.setQuantity(100);
        response.setPrice(new BigDecimal("50.00"));
        response.setOrderType(OrderType.BUY);
        response.setStatus(OrderStatus.PENDING);
        response.setMessage("Order created");

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(accountId, response.getAccountId());
        assertEquals(instrumentId, response.getInstrumentId());
        assertEquals(100, response.getQuantity());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals("Order created", response.getMessage());
    }

    @Test
    @DisplayName("Should handle null message")
    void testCreateOrderResponseNullMessage() {
        // Arrange & Act
        CreateOrderResponse response = new CreateOrderResponse();
        response.setMessage(null);

        // Assert
        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("Should set created at timestamp")
    void testCreateOrderResponseCreatedAt() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        LocalDateTime now = LocalDateTime.now();

        // Act
        response.setCreatedAt(now);

        // Assert
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle different order statuses")
    void testCreateOrderResponseDifferentStatuses() {
        // Arrange & Act & Assert
        CreateOrderResponse response1 = new CreateOrderResponse();
        response1.setStatus(OrderStatus.PENDING);
        assertEquals(OrderStatus.PENDING, response1.getStatus());

        CreateOrderResponse response2 = new CreateOrderResponse();
        response2.setStatus(OrderStatus.FILLED);
        assertEquals(OrderStatus.FILLED, response2.getStatus());
    }
}
