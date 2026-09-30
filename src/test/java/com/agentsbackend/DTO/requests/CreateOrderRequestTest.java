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
    @DisplayName("Should create empty CreateOrderRequest")
    void testCreateOrderRequestEmpty() {
        // Act
        CreateOrderRequest request = new CreateOrderRequest();

        // Assert
        assertNull(request.getAccountId());
        assertNull(request.getInstrumentId());
        assertNull(request.getQuantity());
        assertNull(request.getOrderType());
        assertNull(request.getPrice());
    }

    @Test
    @DisplayName("Should create CreateOrderRequest using constructor with all parameters")
    void testCreateOrderRequestConstructorAllParameters() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        Integer quantity = 150;
        BigDecimal price = new BigDecimal("75.50");
        OrderType orderType = OrderType.SELL;

        // Act
        CreateOrderRequest request = new CreateOrderRequest(accountId, instrumentId, quantity, price, orderType);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(instrumentId, request.getInstrumentId());
        assertEquals(quantity, request.getQuantity());
        assertEquals(price, request.getPrice());
        assertEquals(orderType, request.getOrderType());
    }

    @Test
    @DisplayName("Should create CreateOrderRequest using constructor with null price for market order")
    void testCreateOrderRequestConstructorNullPrice() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        Integer quantity = 100;
        OrderType orderType = OrderType.BUY;

        // Act
        CreateOrderRequest request = new CreateOrderRequest(accountId, instrumentId, quantity, null, orderType);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(instrumentId, request.getInstrumentId());
        assertEquals(quantity, request.getQuantity());
        assertNull(request.getPrice());
        assertEquals(orderType, request.getOrderType());
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
    @DisplayName("Should allow null price for market orders")
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

    @Test
    @DisplayName("Should set and get account ID")
    void testCreateOrderRequestAccountId() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        UUID accountId = UUID.randomUUID();

        // Act
        request.setAccountId(accountId);

        // Assert
        assertEquals(accountId, request.getAccountId());
    }

    @Test
    @DisplayName("Should set and get instrument ID")
    void testCreateOrderRequestInstrumentId() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        UUID instrumentId = UUID.randomUUID();

        // Act
        request.setInstrumentId(instrumentId);

        // Assert
        assertEquals(instrumentId, request.getInstrumentId());
    }

    @Test
    @DisplayName("Should set and get price")
    void testCreateOrderRequestPrice() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        BigDecimal price = new BigDecimal("75.50");

        // Act
        request.setPrice(price);

        // Assert
        assertEquals(price, request.getPrice());
    }

    @Test
    @DisplayName("Should handle minimum quantity of 1")
    void testCreateOrderRequestMinimumQuantity() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setQuantity(1);

        // Assert
        assertEquals(1, request.getQuantity());
    }

    @Test
    @DisplayName("Should handle maximum quantity of 1,000,000")
    void testCreateOrderRequestMaximumQuantity() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setQuantity(1_000_000);

        // Assert
        assertEquals(1_000_000, request.getQuantity());
    }

    @Test
    @DisplayName("Should handle typical quantity values")
    void testCreateOrderRequestTypicalQuantities() {
        // Test various realistic quantities
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setQuantity(10);
        assertEquals(10, request1.getQuantity());

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setQuantity(100);
        assertEquals(100, request2.getQuantity());

        CreateOrderRequest request3 = new CreateOrderRequest();
        request3.setQuantity(10000);
        assertEquals(10000, request3.getQuantity());
    }

    @Test
    @DisplayName("Should handle price precision")
    void testCreateOrderRequestPricePrecision() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        BigDecimal precisePrice = new BigDecimal("49.9999");

        // Act
        request.setPrice(precisePrice);

        // Assert
        assertEquals(precisePrice, request.getPrice());
    }

    @Test
    @DisplayName("Should handle small price values")
    void testCreateOrderRequestSmallPrice() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        BigDecimal smallPrice = new BigDecimal("0.01");

        // Act
        request.setPrice(smallPrice);

        // Assert
        assertEquals(smallPrice, request.getPrice());
    }

    @Test
    @DisplayName("Should handle large price values")
    void testCreateOrderRequestLargePrice() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        BigDecimal largePrice = new BigDecimal("999999.99");

        // Act
        request.setPrice(largePrice);

        // Assert
        assertEquals(largePrice, request.getPrice());
    }

    @Test
    @DisplayName("Should support market BUY order")
    void testCreateOrderRequestMarketBuyOrder() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null); // Market order

        // Assert
        assertEquals(OrderType.BUY, request.getOrderType());
        assertNull(request.getPrice());
        assertEquals(100, request.getQuantity());
    }

    @Test
    @DisplayName("Should support limit SELL order")
    void testCreateOrderRequestLimitSellOrder() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("75.00");

        // Act
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(50);
        request.setPrice(limitPrice); // Limit order

        // Assert
        assertEquals(OrderType.SELL, request.getOrderType());
        assertEquals(limitPrice, request.getPrice());
        assertEquals(50, request.getQuantity());
    }

    @Test
    @DisplayName("Should update all fields independently")
    void testCreateOrderRequestUpdateFields() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        // Act
        request.setAccountId(accountId);
        assertEquals(accountId, request.getAccountId());

        request.setInstrumentId(instrumentId);
        assertEquals(instrumentId, request.getInstrumentId());

        request.setQuantity(100);
        assertEquals(100, request.getQuantity());

        request.setOrderType(OrderType.BUY);
        assertEquals(OrderType.BUY, request.getOrderType());

        request.setPrice(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), request.getPrice());

        // Assert all fields are set correctly
        assertEquals(accountId, request.getAccountId());
        assertEquals(instrumentId, request.getInstrumentId());
        assertEquals(100, request.getQuantity());
        assertEquals(OrderType.BUY, request.getOrderType());
        assertEquals(new BigDecimal("50.00"), request.getPrice());
    }

    @Test
    @DisplayName("Should handle different order types independently")
    void testCreateOrderRequestMultipleOrderTypes() {
        // Arrange
        CreateOrderRequest buyRequest = new CreateOrderRequest();
        CreateOrderRequest sellRequest = new CreateOrderRequest();

        // Act
        buyRequest.setOrderType(OrderType.BUY);
        sellRequest.setOrderType(OrderType.SELL);

        // Assert
        assertEquals(OrderType.BUY, buyRequest.getOrderType());
        assertEquals(OrderType.SELL, sellRequest.getOrderType());
        assertNotEquals(buyRequest.getOrderType(), sellRequest.getOrderType());
    }

    @Test
    @DisplayName("Should handle large order values")
    void testCreateOrderRequestLargeOrderValue() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        BigDecimal largePrice = new BigDecimal("9999999.99");

        // Act
        request.setQuantity(10000);
        request.setPrice(largePrice);

        // Assert
        assertEquals(10000, request.getQuantity());
        assertEquals(largePrice, request.getPrice());
    }

    @Test
    @DisplayName("Should handle null IDs for validation")
    void testCreateOrderRequestNullIds() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setAccountId(null);
        request.setInstrumentId(null);

        // Assert
        assertNull(request.getAccountId());
        assertNull(request.getInstrumentId());
    }

    @Test
    @DisplayName("Should handle null quantity")
    void testCreateOrderRequestNullQuantity() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setQuantity(null);

        // Assert
        assertNull(request.getQuantity());
    }

    @Test
    @DisplayName("Should handle null order type")
    void testCreateOrderRequestNullOrderType() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setOrderType(null);

        // Assert
        assertNull(request.getOrderType());
    }

    @Test
    @DisplayName("Should support order type switching")
    void testCreateOrderRequestOrderTypeSwitching() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setOrderType(OrderType.BUY);
        assertEquals(OrderType.BUY, request.getOrderType());

        request.setOrderType(OrderType.SELL);
        assertEquals(OrderType.SELL, request.getOrderType());

        request.setOrderType(OrderType.BUY);
        assertEquals(OrderType.BUY, request.getOrderType());

        // Assert
        assertEquals(OrderType.BUY, request.getOrderType());
    }

    @Test
    @DisplayName("Should handle price as zero edge case")
    void testCreateOrderRequestPriceZero() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setPrice(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, request.getPrice());
    }

    @Test
    @DisplayName("Should support price field update multiple times")
    void testCreateOrderRequestPriceUpdate() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();

        // Act
        request.setPrice(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), request.getPrice());

        request.setPrice(new BigDecimal("60.00"));
        assertEquals(new BigDecimal("60.00"), request.getPrice());

        request.setPrice(null);
        assertNull(request.getPrice());

        // Assert
        assertNull(request.getPrice());
    }

    @Test
    @DisplayName("Should handle different UUID combinations")
    void testCreateOrderRequestDifferentUUIDs() {
        // Arrange
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID instrumentId1 = UUID.randomUUID();
        UUID instrumentId2 = UUID.randomUUID();

        // Act
        CreateOrderRequest request1 = new CreateOrderRequest(accountId1, instrumentId1, 100, new BigDecimal("50.00"), OrderType.BUY);
        CreateOrderRequest request2 = new CreateOrderRequest(accountId2, instrumentId2, 200, new BigDecimal("75.00"), OrderType.SELL);

        // Assert
        assertEquals(accountId1, request1.getAccountId());
        assertEquals(instrumentId1, request1.getInstrumentId());
        assertEquals(accountId2, request2.getAccountId());
        assertEquals(instrumentId2, request2.getInstrumentId());
        assertNotEquals(request1.getAccountId(), request2.getAccountId());
    }

    @Test
    @DisplayName("Should create multiple independent requests")
    void testCreateOrderRequestMultipleIndependentRequests() {
        // Act
        CreateOrderRequest req1 = new CreateOrderRequest();
        req1.setQuantity(100);

        CreateOrderRequest req2 = new CreateOrderRequest();
        req2.setQuantity(200);

        CreateOrderRequest req3 = new CreateOrderRequest();
        req3.setQuantity(300);

        // Assert
        assertEquals(100, req1.getQuantity());
        assertEquals(200, req2.getQuantity());
        assertEquals(300, req3.getQuantity());
    }

    @Test
    @DisplayName("Should handle quantity edge case values")
    void testCreateOrderRequestQuantityEdgeCases() {
        // Test quantity = 1
        CreateOrderRequest req1 = new CreateOrderRequest();
        req1.setQuantity(1);
        assertEquals(1, req1.getQuantity());

        // Test quantity = 999
        CreateOrderRequest req2 = new CreateOrderRequest();
        req2.setQuantity(999);
        assertEquals(999, req2.getQuantity());

        // Test quantity = 1000
        CreateOrderRequest req3 = new CreateOrderRequest();
        req3.setQuantity(1000);
        assertEquals(1000, req3.getQuantity());

        // Test quantity = 10000
        CreateOrderRequest req4 = new CreateOrderRequest();
        req4.setQuantity(10000);
        assertEquals(10000, req4.getQuantity());
    }
}
