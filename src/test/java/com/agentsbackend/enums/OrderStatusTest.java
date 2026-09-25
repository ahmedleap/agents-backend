package com.agentsbackend.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderStatus Enum Tests")
class OrderStatusTest {

    @Test
    @DisplayName("Should have all expected status values")
    void testOrderStatusValues() {
        // Arrange & Act & Assert
        assertEquals(4, OrderStatus.values().length);
        assertNotNull(OrderStatus.PENDING);
        assertNotNull(OrderStatus.FILLED);
        assertNotNull(OrderStatus.CANCELLED);
        assertNotNull(OrderStatus.REJECTED);
    }

    @Test
    @DisplayName("Should be able to retrieve PENDING status")
    void testPendingStatus() {
        // Arrange & Act
        OrderStatus status = OrderStatus.PENDING;

        // Assert
        assertNotNull(status);
        assertEquals("PENDING", status.name());
    }

    @Test
    @DisplayName("Should be able to retrieve FILLED status")
    void testFilledStatus() {
        // Arrange & Act
        OrderStatus status = OrderStatus.FILLED;

        // Assert
        assertNotNull(status);
        assertEquals("FILLED", status.name());
    }

    @Test
    @DisplayName("Should be able to retrieve CANCELLED status")
    void testCancelledStatus() {
        // Arrange & Act
        OrderStatus status = OrderStatus.CANCELLED;

        // Assert
        assertNotNull(status);
        assertEquals("CANCELLED", status.name());
    }

    @Test
    @DisplayName("Should be able to retrieve REJECTED status")
    void testRejectedStatus() {
        // Arrange & Act
        OrderStatus status = OrderStatus.REJECTED;

        // Assert
        assertNotNull(status);
        assertEquals("REJECTED", status.name());
    }

    @Test
    @DisplayName("Should correctly compare order statuses")
    void testOrderStatusComparison() {
        // Arrange & Act & Assert
        assertEquals(OrderStatus.PENDING, OrderStatus.PENDING);
        assertNotEquals(OrderStatus.PENDING, OrderStatus.FILLED);
        assertNotEquals(OrderStatus.FILLED, OrderStatus.CANCELLED);
        assertNotEquals(OrderStatus.CANCELLED, OrderStatus.REJECTED);
    }
}
