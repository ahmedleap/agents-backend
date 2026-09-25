package com.agentsbackend.DTO.requests;

import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOrderRequest DTO Tests")
class CreateOrderRequestTest {

    @Test
    @DisplayName("Should create CreateOrderRequest with all fields")
    void testCreateOrderRequestInitialization() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        Integer quantity = 100;
        OrderType orderType = OrderType.BUY;
        BigDecimal price = new BigDecimal("50.00");

        // Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setQuantity(quantity);
        request.setOrderType(orderType);
        request.setPrice(price);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(instrumentId, request.getInstrumentId());
        assertEquals(quantity, request.getQuantity());
        assertEquals(orderType, request.getOrderType());
        assertEquals(price, request.getPrice());
    }

    @Test
    @DisplayName("Should handle BUY order type")
    void testCreateOrderRequestBuyOrder() {
        // Arrange & Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(OrderType.BUY);

        // Assert
        assertEquals(OrderType.BUY, request.getOrderType());
    }

    @Test
    @DisplayName("Should handle SELL order type")
    void testCreateOrderRequestSellOrder() {
        // Arrange & Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.SELL, request.getOrderType());
    }

    @Test
    @DisplayName("Should allow null price")
    void testCreateOrderRequestNullPrice() {
        // Arrange & Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPrice(null);

        // Assert
        assertNull(request.getPrice());
    }

    @Test
    @DisplayName("Should set quantity correctly")
    void testCreateOrderRequestQuantity() {
        // Arrange & Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setQuantity(250);

        // Assert
        assertEquals(250, request.getQuantity());
    }
}
