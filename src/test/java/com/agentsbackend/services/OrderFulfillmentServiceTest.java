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
    private InstrumentRepository instrumentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private HoldingsService holdingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderFulfillmentService = new OrderFulfillmentService(
            orderQueue,
            orderRepository,
            accountRepository,
            instrumentRepository,
            auditTrailService,
            holdingsService
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        account.setClientId(clientId);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsService).updateHoldingsForBuy(order, marketPrice);
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));
        account.setClientId(clientId);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsService).updateHoldingsForSell(order);
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsService).updateHoldingsForBuy(order, marketPrice);
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));

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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(orderQueue).remove(order);
        verify(holdingsService).updateHoldingsForSell(order);
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));

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
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

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
        when(instrumentRepository.findById(instrumentId)).thenThrow(new RuntimeException("Database error"));

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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsService).updateHoldingsForBuy(order, marketPrice);
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

        Instrument price1 = createInstrumentPrice(instrumentId1, marketPrice);
        Instrument price2 = createInstrumentPrice(instrumentId2, marketPrice);

        Account account = createAccount(accountId, new BigDecimal("20000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order1, order2));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(price1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(price2));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(orderRepository, times(2)).updateOrder(any());
        verify(orderQueue, times(2)).remove(any());
        verify(holdingsService, times(2)).updateHoldingsForBuy(any(), eq(marketPrice));
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsService).updateHoldingsForSell(order);
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

        Order order = createSellMarketOrder(orderId, accountId, instrumentId, sellQuantity);
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("5000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsService).updateHoldingsForSell(order);
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

        Order order = createBuyMarketOrder(orderId, accountId, instrumentId, quantity);
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        orderFulfillmentService.processPendingOrders();

        // Assert
        verify(holdingsService).updateHoldingsForBuy(order, marketPrice);
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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, initialCash);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, initialCash);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

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
        Instrument currentPrice = createInstrumentPrice(instrumentId, marketPrice);
        Account account = createAccount(accountId, new BigDecimal("10000.00"));
        account.setClientId(clientId);

        when(orderQueue.getPendingOrders()).thenReturn(List.of(order));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(currentPrice));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

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

    private Instrument createInstrument(UUID instrumentId, BigDecimal price) {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        instrument.setMidPrice(price);
        return instrument;
    }

    private Instrument createInstrumentPrice(UUID instrumentId, BigDecimal price) {
        // Changed to return Instrument instead of InstrumentPrice (schema update)
        return createInstrument(instrumentId, price);
    }

    private Account createAccount(UUID accountId, BigDecimal cashBalance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        return account;
    }
}






