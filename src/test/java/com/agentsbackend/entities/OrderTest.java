package com.agentsbackend.entities;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Entity Tests")
class OrderTest {

    @Test
    @DisplayName("Should create Order entity successfully")
    void testOrderCreation() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        Order order = new Order();
        order.setOrderId(orderId);

        // Assert
        assertEquals(orderId, order.getOrderId());
    }

    @Test
    @DisplayName("Should set and get order type")
    void testOrderType() {
        // Arrange & Act
        Order order = new Order();
        order.setOrderType(OrderType.BUY);

        // Assert
        assertEquals(OrderType.BUY, order.getOrderType());
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testOrderQuantity() {
        // Arrange & Act
        Order order = new Order();
        BigDecimal quantity = new BigDecimal("100.00");
        order.setQuantity(quantity);

        // Assert
        assertEquals(quantity, order.getQuantity());
    }

    @Test
    @DisplayName("Should set and get status")
    void testOrderStatus() {
        // Arrange & Act
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        // Assert
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Should set and get limit price")
    void testOrderLimitPrice() {
        // Arrange & Act
        Order order = new Order();
        BigDecimal limitPrice = new BigDecimal("50.00");
        order.setLimitPrice(limitPrice);

        // Assert
        assertEquals(limitPrice, order.getLimitPrice());
    }

    @Test
    @DisplayName("Should set and get filled price")
    void testOrderFilledPrice() {
        // Arrange & Act
        Order order = new Order();
        BigDecimal filledPrice = new BigDecimal("51.00");
        order.setFilledPrice(filledPrice);

        // Assert
        assertEquals(filledPrice, order.getFilledPrice());
    }

    @Test
    @DisplayName("Should set and get created at timestamp")
    void testOrderCreatedAt() {
        // Arrange
        Order order = new Order();
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        order.setCreatedAt(createdAt);

        // Assert
        assertEquals(createdAt, order.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get filled at timestamp")
    void testOrderFilledAt() {
        // Arrange
        Order order = new Order();
        LocalDateTime filledAt = LocalDateTime.now();

        // Act
        order.setFilledAt(filledAt);

        // Assert
        assertEquals(filledAt, order.getFilledAt());
    }

    @Test
    @DisplayName("Should set and get cancelled at timestamp")
    void testOrderCancelledAt() {
        // Arrange
        Order order = new Order();
        LocalDateTime cancelledAt = LocalDateTime.now();

        // Act
        order.setCancelledAt(cancelledAt);

        // Assert
        assertEquals(cancelledAt, order.getCancelledAt());
    }

    @Test
    @DisplayName("Should set and get account")
    void testOrderAccount() {
        // Arrange
        Order order = new Order();
        Account account = new Account();

        // Act
        order.setAccount(account);

        // Assert
        assertEquals(account, order.getAccount());
    }

    @Test
    @DisplayName("Should set and get instrument")
    void testOrderInstrument() {
        // Arrange
        Order order = new Order();
        Instrument instrument = new Instrument();

        // Act
        order.setInstrument(instrument);

        // Assert
        assertEquals(instrument, order.getInstrument());
    }

    @Test
    @DisplayName("Should handle SELL order type")
    void testOrderTypeSell() {
        // Arrange & Act
        Order order = new Order();
        order.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.SELL, order.getOrderType());
    }

    @Test
    @DisplayName("Should handle FILLED status")
    void testOrderStatusFilled() {
        // Arrange & Act
        Order order = new Order();
        order.setStatus(OrderStatus.FILLED);

        // Assert
        assertEquals(OrderStatus.FILLED, order.getStatus());
    }

    @Test
    @DisplayName("Should handle CANCELLED status")
    void testOrderStatusCancelled() {
        // Arrange & Act
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
}
