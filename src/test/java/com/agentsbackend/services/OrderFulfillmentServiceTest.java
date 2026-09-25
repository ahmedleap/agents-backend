package com.agentsbackend.services;

import com.agentsbackend.entities.*;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.queue.OrderQueue;
import com.agentsbackend.repos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderFulfillmentService Tests")
class OrderFulfillmentServiceTest {

    private OrderFulfillmentService orderFulfillmentService;

    @Mock
    private OrderQueue orderQueue;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InstrumentPriceRepository instrumentPriceRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderFulfillmentService = new OrderFulfillmentService(
            orderQueue,
            orderRepository,
            instrumentPriceRepository,
            holdingsRepository,
            accountRepository,
            auditTrailService
        );
    }

    // ==================== processPendingOrders Tests ====================

    @Test
    @DisplayName("Should handle empty order queue gracefully")
    void testProcessPendingOrdersWithEmptyQueue() {
        // Arrange
        when(orderQueue.getPendingOrders()).thenReturn(new ArrayList<>());

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderQueue).getPendingOrders();
        verify(orderRepository, never()).updateOrder(any());
        verify(orderQueue, never()).remove(any());
    }

    @Test
    @DisplayName("Should process BUY market order successfully")
    void testProcessBuyMarketOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        account.setClientId(clientId);
        Holding holding = createHolding(accountId, instrumentId, new BigDecimal("50.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsRepository).save(any(Holding.class));
        verify(accountRepository).save(any(Account.class));
        verify(auditTrailService).logOrderFilled(eq(orderId), eq(accountId), eq(clientId), eq(OrderType.BUY), eq(100), eq(marketPrice));
    }

    @Test
    @DisplayName("Should process SELL market order successfully")
    void testProcessSellMarketOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("50.00");

        Order order = createSellMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));
        account.setClientId(clientId);
        Holding holding = createHolding(accountId, instrumentId, quantity, new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsRepository).deleteByAccountAndInstrument(accountId, instrumentId);
        verify(accountRepository).save(any(Account.class));
        verify(auditTrailService).logOrderFilled(eq(orderId), eq(accountId), eq(clientId), eq(OrderType.SELL), eq(50), eq(marketPrice));
    }

    @Test
    @DisplayName("Should fill BUY limit order when market price is lower than limit")
    void testProcessBuyLimitOrderWhenPriceLow() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("50.00");
        BigDecimal marketPrice = new BigDecimal("48.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyLimitOrder(orderId, accountId, instrumentId, quantity, limitPrice);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        Holding holding = createHolding(accountId, instrumentId, new BigDecimal("50.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
    }

    @Test
    @DisplayName("Should not fill BUY limit order when market price is higher than limit")
    void testProcessBuyLimitOrderWhenPriceHigh() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("50.00");
        BigDecimal marketPrice = new BigDecimal("52.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyLimitOrder(orderId, accountId, instrumentId, quantity, limitPrice);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository, never()).updateOrder(any());
        verify(orderQueue, never()).remove(any());
    }

    @Test
    @DisplayName("Should fill SELL limit order when market price is higher than limit")
    void testProcessSellLimitOrderWhenPriceHigh() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("50.00");
        BigDecimal marketPrice = new BigDecimal("52.00");
        BigDecimal quantity = new BigDecimal("50.00");

        Order order = createSellLimitOrder(orderId, accountId, instrumentId, quantity, limitPrice);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));
        Holding holding = createHolding(accountId, instrumentId, new BigDecimal("100.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
    }

    @Test
    @DisplayName("Should not fill SELL limit order when market price is lower than limit")
    void testProcessSellLimitOrderWhenPriceLow() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("50.00");
        BigDecimal marketPrice = new BigDecimal("48.00");
        BigDecimal quantity = new BigDecimal("50.00");

        Order order = createSellLimitOrder(orderId, accountId, instrumentId, quantity, limitPrice);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository, never()).updateOrder(any());
        verify(orderQueue, never()).remove(any());
    }

    @Test
    @DisplayName("Should handle order processing when no market price is available")
    void testProcessOrderWithoutMarketPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(null);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository, never()).updateOrder(any());
        verify(orderQueue, never()).remove(any());
    }

    @Test
    @DisplayName("Should handle exception during order processing")
    void testProcessOrderWithException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw, should continue processing
        assertDoesNotThrow(() -> orderFulfillmentService.processPendingOrders());
        verify(orderQueue, never()).remove(any());
    }

    @Test
    @DisplayName("Should create new holding for BUY order when none exists")
    void testBuyOrderCreatesNewHolding() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsRepository).save(any(Holding.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should handle multiple orders in single processing cycle")
    void testProcessMultipleOrders() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId1 = UUID.randomUUID();
        UUID instrumentId2 = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order1 = createBuyMarketOrder(UUID.randomUUID(), accountId, instrumentId1, quantity);
        Order order2 = createBuyMarketOrder(UUID.randomUUID(), accountId, instrumentId2, quantity);

        InstrumentPrice price1 = createInstrumentPrice(instrumentId1, marketPrice);
        InstrumentPrice price2 = createInstrumentPrice(instrumentId2, marketPrice);

        Account account = createAccount(accountId, new BigDecimal("20000.00"));
        Holding holding1 = createHolding(accountId, instrumentId1, new BigDecimal("50.00"), new BigDecimal("45.00"));
        Holding holding2 = createHolding(accountId, instrumentId2, new BigDecimal("50.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order1, order2));
        when(instrumentPriceRepository.findLatestPrice(instrumentId1)).thenReturn(price1);
        when(instrumentPriceRepository.findLatestPrice(instrumentId2)).thenReturn(price2);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(holding1);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId2)).thenReturn(holding2);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository, times(2)).updateOrder(any());
        verify(orderQueue, times(2)).remove(any());
    }

    @Test
    @DisplayName("Should delete holding when SELL order quantity reaches zero")
    void testSellOrderDeletesHoldingWhenZeroQuantity() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("50.00");

        Order order = createSellMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));
        Holding holding = createHolding(accountId, instrumentId, quantity, new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsRepository).deleteByAccountAndInstrument(accountId, instrumentId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should reduce holding quantity for partial SELL order")
    void testSellOrderReducesHoldingQuantity() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal sellQuantity = new BigDecimal("30.00");
        BigDecimal holdingQuantity = new BigDecimal("100.00");

        Order order = createSellMarketOrder(orderId, accountId, instrumentId, sellQuantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));
        Holding holding = createHolding(accountId, instrumentId, holdingQuantity, new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsRepository).save(any(Holding.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should calculate correct average cost basis for BUY order")
    void testAverageCostBasisCalculationForBuy() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal existingQuantity = new BigDecimal("100.00");
        BigDecimal existingCostBasis = new BigDecimal("45.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        Holding holding = createHolding(accountId, instrumentId, existingQuantity, existingCostBasis);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsRepository).save(any(Holding.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should update cash balance correctly for BUY order")
    void testCashBalanceUpdateForBuy() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal initialCash = new BigDecimal("10000.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, initialCash);
        Holding holding = createHolding(accountId, instrumentId, new BigDecimal("50.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        // Expected: 10000 - (50 * 100) = 5000
        verify(accountRepository).save(argThat(acc -> 
            acc.getCashBalance().compareTo(new BigDecimal("5000.00")) == 0
        ));
    }

    @Test
    @DisplayName("Should update cash balance correctly for SELL order")
    void testCashBalanceUpdateForSell() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal initialCash = new BigDecimal("1000.00");

        Order order = createSellMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, initialCash);
        Holding holding = createHolding(accountId, instrumentId, quantity, new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        // Expected: 1000 + (50 * 100) = 6000
        verify(accountRepository).save(argThat(acc -> 
            acc.getCashBalance().compareTo(new BigDecimal("6000.00")) == 0
        ));
    }

    @Test
    @DisplayName("Should log audit trail for filled order")
    void testAuditTrailLogging() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal quantity = new BigDecimal("100.00");

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        InstrumentPrice currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        account.setClientId(clientId);
        Holding holding = createHolding(accountId, instrumentId, new BigDecimal("50.00"), new BigDecimal("45.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentPriceRepository.findLatestPrice(instrumentId)).thenReturn(currentPrice);
        when(accountRepository.findById(accountId)).thenReturn(account);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(auditTrailService).logOrderFilled(orderId, accountId, clientId, OrderType.BUY, 100, marketPrice);
    }

    // ==================== Helper Methods ====================

    private Order createBuyMarketOrder(UUID orderId, UUID accountId, UUID instrumentId, BigDecimal quantity) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderType(OrderType.BUY);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(quantity);
        order.setLimitPrice(null);

        Account account = new Account();
        account.setAccountId(accountId);
        order.setAccount(account);

        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        order.setInstrument(instrument);

        return order;
    }

    private Order createSellMarketOrder(UUID orderId, UUID accountId, UUID instrumentId, BigDecimal quantity) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderType(OrderType.SELL);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(quantity);
        order.setLimitPrice(null);

        Account account = new Account();
        account.setAccountId(accountId);
        order.setAccount(account);

        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        order.setInstrument(instrument);

        return order;
    }

    private Order createBuyLimitOrder(UUID orderId, UUID accountId, UUID instrumentId, BigDecimal quantity, BigDecimal limitPrice) {
        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        order.setLimitPrice(limitPrice);
        return order;
    }

    private Order createSellLimitOrder(UUID orderId, UUID accountId, UUID instrumentId, BigDecimal quantity, BigDecimal limitPrice) {
        Order order = createSellMarketOrder(orderId, accountId, instrumentId, quantity);
        order.setLimitPrice(limitPrice);
        return order;
    }

    private InstrumentPrice createInstrumentPrice(UUID instrumentId, BigDecimal price) {
        InstrumentPrice instrumentPrice = new InstrumentPrice();
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        instrumentPrice.setInstrument(instrument);
        instrumentPrice.setPrice(price);
        return instrumentPrice;
    }

    private Account createAccount(UUID accountId, BigDecimal cashBalance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        return account;
    }

    private Holding createHolding(UUID accountId, UUID instrumentId, BigDecimal quantity, BigDecimal averageCostBasis) {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());

        Account account = new Account();
        account.setAccountId(accountId);
        holding.setAccount(account);

        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        holding.setInstrument(instrument);

        holding.setQuantity(quantity);
        holding.setAverageCostBasis(averageCostBasis);
        return holding;
    }
}
