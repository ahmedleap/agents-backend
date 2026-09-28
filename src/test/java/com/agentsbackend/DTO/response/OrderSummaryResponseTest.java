package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderSummaryResponse DTO Tests")
class OrderSummaryResponseTest {

    @Test
    @DisplayName("Should create OrderSummaryResponse with all fields")
    void testOrderSummaryResponseInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderType orderType = OrderType.BUY;
        Integer quantity = 100;
        BigDecimal limitPrice = new BigDecimal("50.00");
        BigDecimal filledPrice = new BigDecimal("48.50");
        OrderStatus status = OrderStatus.FILLED;

        // Act
        OrderSummaryResponse response = new OrderSummaryResponse(
            orderId, orderType, quantity, limitPrice, filledPrice, status
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(orderType, response.getOrderType());
        assertEquals(quantity, response.getQuantity());
        assertEquals(limitPrice, response.getLimitPrice());
        assertEquals(filledPrice, response.getFilledPrice());
        assertEquals(status, response.getStatus());
    }

    @Test
    @DisplayName("Should create empty OrderSummaryResponse")
    void testOrderSummaryResponseEmpty() {
        // Act
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Assert
        assertNull(response.getOrderId());
        assertNull(response.getOrderType());
        assertNull(response.getQuantity());
        assertNull(response.getLimitPrice());
        assertNull(response.getFilledPrice());
        assertNull(response.getStatus());
    }

    @Test
    @DisplayName("Should set and get order ID")
    void testOrderSummaryResponseOrderId() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        UUID orderId = UUID.randomUUID();

        // Act
        response.setOrderId(orderId);

        // Assert
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("Should set and get order type")
    void testOrderSummaryResponseOrderType() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Act
        response.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.SELL, response.getOrderType());
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testOrderSummaryResponseQuantity() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        Integer quantity = 250;

        // Act
        response.setQuantity(quantity);

        // Assert
        assertEquals(quantity, response.getQuantity());
    }

    @Test
    @DisplayName("Should set and get limit price")
    void testOrderSummaryResponseLimitPrice() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        BigDecimal limitPrice = new BigDecimal("75.50");

        // Act
        response.setLimitPrice(limitPrice);

        // Assert
        assertEquals(limitPrice, response.getLimitPrice());
    }

    @Test
    @DisplayName("Should set and get filled price")
    void testOrderSummaryResponseFilledPrice() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        BigDecimal filledPrice = new BigDecimal("72.25");

        // Act
        response.setFilledPrice(filledPrice);

        // Assert
        assertEquals(filledPrice, response.getFilledPrice());
    }

    @Test
    @DisplayName("Should set and get status")
    void testOrderSummaryResponseStatus() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Act
        response.setStatus(OrderStatus.PENDING);

        // Assert
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should handle null prices")
    void testOrderSummaryResponseNullPrices() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Act
        response.setLimitPrice(null);
        response.setFilledPrice(null);

        // Assert
        assertNull(response.getLimitPrice());
        assertNull(response.getFilledPrice());
    }

    @Test
    @DisplayName("Should handle different order types")
    void testOrderSummaryResponseDifferentOrderTypes() {
        // Arrange
        OrderSummaryResponse buyResponse = new OrderSummaryResponse();
        OrderSummaryResponse sellResponse = new OrderSummaryResponse();

        // Act
        buyResponse.setOrderType(OrderType.BUY);
        sellResponse.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.BUY, buyResponse.getOrderType());
        assertEquals(OrderType.SELL, sellResponse.getOrderType());
    }

    @Test
    @DisplayName("Should handle all order statuses")
    void testOrderSummaryResponseAllStatuses() {
        // Arrange
        OrderSummaryResponse response1 = new OrderSummaryResponse();
        OrderSummaryResponse response2 = new OrderSummaryResponse();
        OrderSummaryResponse response3 = new OrderSummaryResponse();
        OrderSummaryResponse response4 = new OrderSummaryResponse();

        // Act
        response1.setStatus(OrderStatus.PENDING);
        response2.setStatus(OrderStatus.FILLED);
        response3.setStatus(OrderStatus.CANCELLED);
        response4.setStatus(OrderStatus.REJECTED);

        // Assert
        assertEquals(OrderStatus.PENDING, response1.getStatus());
        assertEquals(OrderStatus.FILLED, response2.getStatus());
        assertEquals(OrderStatus.CANCELLED, response3.getStatus());
        assertEquals(OrderStatus.REJECTED, response4.getStatus());
    }

    @Test
    @DisplayName("Should create summary for filled BUY order")
    void testOrderSummaryResponseFilledBuyOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        OrderSummaryResponse response = new OrderSummaryResponse(
            orderId, OrderType.BUY, 100, new BigDecimal("50.00"), 
            new BigDecimal("48.00"), OrderStatus.FILLED
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(100, response.getQuantity());
        assertNotNull(response.getLimitPrice());
        assertNotNull(response.getFilledPrice());
        assertEquals(OrderStatus.FILLED, response.getStatus());
    }

    @Test
    @DisplayName("Should create summary for pending market order")
    void testOrderSummaryResponsePendingMarketOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        OrderSummaryResponse response = new OrderSummaryResponse(
            orderId, OrderType.BUY, 50, null, null, OrderStatus.PENDING
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertNull(response.getLimitPrice());
        assertNull(response.getFilledPrice());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should update all fields")
    void testOrderSummaryResponseUpdateFields() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        UUID orderId = UUID.randomUUID();

        // Act
        response.setOrderId(orderId);
        response.setOrderType(OrderType.BUY);
        response.setQuantity(100);
        response.setLimitPrice(new BigDecimal("50.00"));
        response.setFilledPrice(new BigDecimal("49.00"));
        response.setStatus(OrderStatus.FILLED);

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(100, response.getQuantity());
        assertEquals(new BigDecimal("50.00"), response.getLimitPrice());
        assertEquals(new BigDecimal("49.00"), response.getFilledPrice());
        assertEquals(OrderStatus.FILLED, response.getStatus());
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testOrderSummaryResponseZeroQuantity() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Act
        response.setQuantity(0);

        // Assert
        assertEquals(0, response.getQuantity());
    }

    @Test
    @DisplayName("Should handle large quantity")
    void testOrderSummaryResponseLargeQuantity() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();

        // Act
        response.setQuantity(10000);

        // Assert
        assertEquals(10000, response.getQuantity());
    }

    @Test
    @DisplayName("Should handle precision in prices")
    void testOrderSummaryResponsePricePrecision() {
        // Arrange
        OrderSummaryResponse response = new OrderSummaryResponse();
        BigDecimal precisePrice = new BigDecimal("49.9999");

        // Act
        response.setLimitPrice(precisePrice);

        // Assert
        assertEquals(precisePrice, response.getLimitPrice());
    }
}
