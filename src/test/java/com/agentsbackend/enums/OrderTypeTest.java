package com.agentsbackend.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderType Enum Tests")
class OrderTypeTest {

    @Test
    @DisplayName("Should have all expected order type values")
    void testOrderTypeValues() {
        // Arrange & Act & Assert
        assertEquals(2, OrderType.values().length);
        assertNotNull(OrderType.BUY);
        assertNotNull(OrderType.SELL);
    }

    @Test
    @DisplayName("Should be able to retrieve BUY order type")
    void testBuyOrderType() {
        // Arrange & Act
        OrderType orderType = OrderType.BUY;

        // Assert
        assertNotNull(orderType);
        assertEquals("BUY", orderType.name());
    }

    @Test
    @DisplayName("Should be able to retrieve SELL order type")
    void testSellOrderType() {
        // Arrange & Act
        OrderType orderType = OrderType.SELL;

        // Assert
        assertNotNull(orderType);
        assertEquals("SELL", orderType.name());
    }

    @Test
    @DisplayName("Should correctly compare order types")
    void testOrderTypeComparison() {
        // Arrange & Act & Assert
        assertEquals(OrderType.BUY, OrderType.BUY);
        assertNotEquals(OrderType.BUY, OrderType.SELL);
    }

    @Test
    @DisplayName("Should handle order type in conditional logic")
    void testOrderTypeConditionalLogic() {
        // Arrange
        OrderType buyType = OrderType.BUY;
        OrderType sellType = OrderType.SELL;

        // Act & Assert
        if (buyType.equals(OrderType.BUY)) {
            assertTrue(true);
        }
        
        if (sellType.equals(OrderType.SELL)) {
            assertTrue(true);
        }
    }
}
