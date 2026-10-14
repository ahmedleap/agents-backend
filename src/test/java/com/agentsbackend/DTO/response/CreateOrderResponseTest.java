package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        // Act
        CreateOrderResponse response = new CreateOrderResponse(
            orderId, accountId, instrumentId, 100, new BigDecimal("50.00"),
            new BigDecimal("5000.00"), OrderType.BUY, OrderStatus.PENDING,
            createdAt, "Order created"
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(accountId, response.getAccountId());
        assertEquals(instrumentId, response.getInstrumentId());
        assertEquals(100, response.getQuantity());
        assertEquals(new BigDecimal("50.00"), response.getPrice());
        assertEquals(new BigDecimal("5000.00"), response.getTotalValue());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals("Order created", response.getMessage());
    }

    @Test
    @DisplayName("Should create empty CreateOrderResponse")
    void testCreateOrderResponseEmpty() {
        // Act
        CreateOrderResponse response = new CreateOrderResponse();

        // Assert
        assertNull(response.getOrderId());
        assertNull(response.getAccountId());
        assertNull(response.getInstrumentId());
        assertNull(response.getQuantity());
        assertNull(response.getPrice());
        assertNull(response.getTotalValue());
        assertNull(response.getOrderType());
        assertNull(response.getStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getMessage());
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
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

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

        CreateOrderResponse response3 = new CreateOrderResponse();
        response3.setStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, response3.getStatus());

        CreateOrderResponse response4 = new CreateOrderResponse();
        response4.setStatus(OrderStatus.REJECTED);
        assertEquals(OrderStatus.REJECTED, response4.getStatus());
    }

    @Test
    @DisplayName("Should set and get total value")
    void testCreateOrderResponseTotalValue() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        BigDecimal totalValue = new BigDecimal("7500.00");

        // Act
        response.setTotalValue(totalValue);

        // Assert
        assertEquals(totalValue, response.getTotalValue());
    }

    @Test
    @DisplayName("Should handle null price for market orders")
    void testCreateOrderResponseNullPrice() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();

        // Act
        response.setPrice(null);

        // Assert
        assertNull(response.getPrice());
    }

    @Test
    @DisplayName("Should handle null total value for market orders")
    void testCreateOrderResponseNullTotalValue() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();

        // Act
        response.setTotalValue(null);

        // Assert
        assertNull(response.getTotalValue());
    }

    @Test
    @DisplayName("Should set and get all IDs")
    void testCreateOrderResponseIds() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        response.setOrderId(orderId);
        response.setAccountId(accountId);
        response.setInstrumentId(instrumentId);

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(accountId, response.getAccountId());
        assertEquals(instrumentId, response.getInstrumentId());
    }

    @Test
    @DisplayName("Should handle different order types")
    void testCreateOrderResponseOrderTypes() {
        // Arrange
        CreateOrderResponse buyResponse = new CreateOrderResponse();
        CreateOrderResponse sellResponse = new CreateOrderResponse();

        // Act
        buyResponse.setOrderType(OrderType.BUY);
        sellResponse.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.BUY, buyResponse.getOrderType());
        assertEquals(OrderType.SELL, sellResponse.getOrderType());
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testCreateOrderResponseQuantity() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();

        // Act
        response.setQuantity(250);

        // Assert
        assertEquals(250, response.getQuantity());
    }

    @Test
    @DisplayName("Should set and get message")
    void testCreateOrderResponseMessage() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        String message = "Order created successfully and queued for fulfillment";

        // Act
        response.setMessage(message);

        // Assert
        assertEquals(message, response.getMessage());
    }

    @Test
    @DisplayName("Should calculate correct total value")
    void testCreateOrderResponseCalculateTotalValue() {
        // Arrange
        BigDecimal price = new BigDecimal("50.00");
        Integer quantity = 100;
        BigDecimal expectedTotal = price.multiply(new BigDecimal(quantity));

        // Act
        CreateOrderResponse response = new CreateOrderResponse();
        response.setPrice(price);
        response.setQuantity(quantity);
        response.setTotalValue(expectedTotal);

        // Assert
        assertEquals(expectedTotal, response.getTotalValue());
    }

    @Test
    @DisplayName("Should handle precision in prices")
    void testCreateOrderResponsePricePrecision() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        BigDecimal precisePrice = new BigDecimal("49.9999");

        // Act
        response.setPrice(precisePrice);

        // Assert
        assertEquals(precisePrice, response.getPrice());
    }

    @Test
    @DisplayName("Should handle large quantities")
    void testCreateOrderResponseLargeQuantity() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();

        // Act
        response.setQuantity(10000);

        // Assert
        assertEquals(10000, response.getQuantity());
    }

    @Test
    @DisplayName("Should update all fields independently")
    void testCreateOrderResponseUpdateFields() {
        // Arrange
        CreateOrderResponse response = new CreateOrderResponse();
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        // Act
        response.setOrderId(orderId);
        response.setAccountId(accountId);
        response.setQuantity(100);
        response.setPrice(new BigDecimal("50.00"));
        response.setTotalValue(new BigDecimal("5000.00"));
        response.setOrderType(OrderType.BUY);
        response.setStatus(OrderStatus.PENDING);
        response.setMessage("Test");

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(accountId, response.getAccountId());
        assertEquals(100, response.getQuantity());
        assertEquals(new BigDecimal("50.00"), response.getPrice());
        assertEquals(new BigDecimal("5000.00"), response.getTotalValue());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals("Test", response.getMessage());
    }

    @Test
    @DisplayName("Should create response for BUY limit order")
    void testCreateOrderResponseBuyLimitOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        CreateOrderResponse response = new CreateOrderResponse(
            orderId, accountId, instrumentId, 100, new BigDecimal("50.00"),
            new BigDecimal("5000.00"), OrderType.BUY, OrderStatus.PENDING,
            OffsetDateTime.now(ZoneOffset.UTC), "Limit order created"
        );

        // Assert
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(new BigDecimal("50.00"), response.getPrice());
    }

    @Test
    @DisplayName("Should create response for SELL market order")
    void testCreateOrderResponseSellMarketOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        CreateOrderResponse response = new CreateOrderResponse(
            orderId, accountId, instrumentId, 50, null,
            new BigDecimal("2500.00"), OrderType.SELL, OrderStatus.PENDING,
            OffsetDateTime.now(ZoneOffset.UTC), "Market order created"
        );

        // Assert
        assertEquals(OrderType.SELL, response.getOrderType());
        assertNull(response.getPrice());
        assertNotNull(response.getTotalValue());
    }
}
