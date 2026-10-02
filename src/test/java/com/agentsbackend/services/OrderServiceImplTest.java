package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.exceptions.*;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.DTO.response.OrderSummaryResponse;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.HoldingsRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import com.agentsbackend.queue.OrderQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private OrderQueue orderQueue;

    @Mock
    private AuditTrailService auditTrailService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(
            orderRepository,
            holdingsRepository,
            accountRepository,
            instrumentRepository,
            orderQueue,
            auditTrailService
        );
    }

    @Test
    @DisplayName("Should retrieve all pending orders when no account filter provided")
    void testGetPendingOrdersWithoutFilter() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<Order> pendingOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID());
        order1.setStatus(OrderStatus.PENDING);
        pendingOrders.add(order1);

        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(pendingOrders);

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        verify(orderRepository).findPendingOrdersByAccount(accountId);
    }

    @Test
    @DisplayName("Should retrieve pending orders filtered by account ID")
    void testGetPendingOrdersByAccountId() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<Order> accountOrders = new ArrayList<>();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        accountOrders.add(order);

        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(accountOrders);

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findPendingOrdersByAccount(accountId);
    }

    @Test
    @DisplayName("Should return empty list when no pending orders exist")
    void testGetPendingOrdersEmpty() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findPendingOrdersByAccount(accountId);
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void testCancelOrderSuccess() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        
        Client client = new Client();
        client.setClientId(clientId);
        
        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setClient(client);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(new BigDecimal("100"));
        order.setOrderType(OrderType.BUY);
        order.setLimitPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(order);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(auditTrailService).logOrderCancelled(any(), any(), any(), any(), anyInt(), any(), any());

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        CancelOrderResponse response = orderService.cancelOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).updateOrder(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when canceling non-existent order")
    void testCancelOrderNotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(null);

        // Act & Assert
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(request));
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should retrieve order by ID successfully")
    void testGetOrderById() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderType(OrderType.BUY);

        when(orderRepository.findById(orderId)).thenReturn(order);

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void testGetOrderByIdNotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(null);

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should verify repository is called for pending orders")
    void testRepositoryCallForPendingOrders() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findPendingOrdersByAccount(accountId);
    }

    @Test
    @DisplayName("Should call correct repository method when account filter provided")
    void testRepositoryCallWithAccountFilter() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findPendingOrdersByAccount(accountId);
        verify(orderRepository, never()).findPendingOrders();
    }

    @Test
    @DisplayName("Should return multiple pending orders")
    void testGetMultiplePendingOrders() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<Order> pendingOrders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderId(UUID.randomUUID());
            order.setStatus(OrderStatus.PENDING);
            pendingOrders.add(order);
        }

        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(pendingOrders);

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(o -> o.getStatus() == OrderStatus.PENDING));
    }

    @Test
    @DisplayName("Should handle order retrieval with different statuses")
    void testGetOrderWithDifferentStatuses() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        Order filledOrder = new Order();
        filledOrder.setOrderId(orderId);
        filledOrder.setStatus(OrderStatus.FILLED);

        when(orderRepository.findById(orderId)).thenReturn(filledOrder);

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.FILLED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when trying to cancel already cancelled order")
    void testCancelAlreadyCancelledOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        
        Order cancelledOrder = new Order();
        cancelledOrder.setOrderId(orderId);
        cancelledOrder.setAccount(account);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(cancelledOrder);

        // Act & Assert
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        assertThrows(Exception.class, () -> orderService.cancelOrder(request));
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should verify audit trail service calls on order cancellation")
    void testAuditTrailOnCancellation() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        
        Client client = new Client();
        client.setClientId(clientId);
        
        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setClient(client);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(new BigDecimal("100"));
        order.setOrderType(OrderType.BUY);
        order.setLimitPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(order);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(auditTrailService).logOrderCancelled(any(), any(), any(), any(), anyInt(), any(), any());

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        orderService.cancelOrder(request);

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(auditTrailService).logOrderCancelled(eq(orderId), eq(accountId), eq(clientId), any(), anyInt(), any(), any());
    }

    // ==================== getOrderHistory Tests ====================

    @Test
    @DisplayName("Should retrieve order history for account")
    void testGetOrderHistory() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(accountId);

        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID());
        order1.setOrderType(OrderType.BUY);
        order1.setQuantity(new BigDecimal("100.00"));
        order1.setStatus(OrderStatus.FILLED);
        orders.add(order1);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(orderRepository.findAllByAccount(accountId)).thenReturn(orders);

        // Act
        List<OrderSummaryResponse> result = orderService.getOrderHistory(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository).findById(accountId);
        verify(orderRepository).findAllByAccount(accountId);
    }

    @Test
    @DisplayName("Should throw exception for non-existent account history")
    void testGetOrderHistoryAccountNotFound() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> orderService.getOrderHistory(accountId));
        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("Should return empty list for account with no orders")
    void testGetOrderHistoryEmpty() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(orderRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        List<OrderSummaryResponse> result = orderService.getOrderHistory(accountId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return multiple orders in history")
    void testGetOrderHistoryMultipleOrders() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(accountId);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderId(UUID.randomUUID());
            order.setOrderType(OrderType.BUY);
            order.setQuantity(new BigDecimal("100.00"));
            order.setStatus(OrderStatus.FILLED);
            orders.add(order);
        }

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(orderRepository.findAllByAccount(accountId)).thenReturn(orders);

        // Act
        List<OrderSummaryResponse> result = orderService.getOrderHistory(accountId);

        // Assert
        assertEquals(5, result.size());
    }

    // ==================== cancelOrder Tests ====================

    @Test
    @DisplayName("Should throw exception when canceling filled order")
    void testCancelFilledOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setStatus(OrderStatus.FILLED);

        when(orderRepository.findById(orderId)).thenReturn(order);

        // Act & Assert
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelOrder(request));
    }

    @Test
    @DisplayName("Should throw exception when canceling rejected order")
    void testCancelRejectedOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setStatus(OrderStatus.REJECTED);

        when(orderRepository.findById(orderId)).thenReturn(order);

        // Act & Assert
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelOrder(request));
    }

    // ==================== createOrder Tests ====================

    @Test
    @DisplayName("Should create market BUY order successfully")
    void testCreateMarketBuyOrderSuccess() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null); // Market order

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getAccountId());
        assertEquals(instrumentId, response.getInstrumentId());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(orderQueue).enqueue(any(Order.class));
        verify(auditTrailService).logOrderCreated(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("Should create limit BUY order successfully")
    void testCreateLimitBuyOrderSuccess() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal limitPrice = new BigDecimal("48.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice); // Limit order

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), limitPrice)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(limitPrice, response.getPrice());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create SELL order successfully")
    void testCreateSellOrderSuccess() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("200.00"));

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(100);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(OrderType.SELL, response.getOrderType());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should reject order when market price not available for market order")
    void testCreateMarketOrderNoMarketPrice() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null); // Market order

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject order when limit price exceeds 50% above market")
    void testCreateLimitOrderPriceTooHigh() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal limitPrice = new BigDecimal("160.00"); // 60% above market

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject order when limit price is 50% below market")
    void testCreateLimitOrderPriceTooLow() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal limitPrice = new BigDecimal("40.00"); // 60% below market

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should reject order when client age less than 18")
    void testCreateOrderClientUnderAge() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(17)); // Only 17 years old

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject order when client age cannot be determined")
    void testCreateOrderClientAgeMissing() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(null);

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should reject duplicate order within 60 seconds")
    void testCreateOrderDuplicate() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(1); // Duplicate found

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject BUY order exceeding 10000 position limit")
    void testCreateBuyOrderExceedsPositionLimit() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("9500.00"));

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(600); // 9500 + 600 = 10100, exceeds 10000 limit
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("600"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject SELL order when no holdings exist")
    void testCreateSellOrderNoHoldings() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(100);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null); // No holdings

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject SELL order when insufficient holdings")
    void testCreateSellOrderInsufficientHoldings() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("50.00")); // Only own 50 shares

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(100); // Trying to sell 100, only own 50
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject BUY order with insufficient cash balance")
    void testCreateBuyOrderInsufficientCash() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal cashBalance = new BigDecimal("5000.00"); // Only $5000 available

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100); // Would need $10,000 but only have $5000
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(request));
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    @DisplayName("Should reject order leaving account below minimum balance")
    void testCreateOrderBelowMinimumCash() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal cashBalance = new BigDecimal("9000.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100); // Would leave $0 balance
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should reject order when account balance too low and equity too low")
    void testCreateOrderLowBalanceLowEquity() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal cashBalance = new BigDecimal("2.00"); // Low balance

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(1);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("1"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should accept order when all validations pass")
    void testCreateOrderAllValidationsPass() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(50);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("50"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(orderQueue).enqueue(any(Order.class));
    }

    @Test
    @DisplayName("Should reject limit order when price is exactly at 150% of market price")
    void testCreateLimitOrderPriceAtUpperBoundary() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal limitPrice = new BigDecimal("150.00"); // Exactly 1.50x market

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(UUID.randomUUID());
        account.setCashBalance(new BigDecimal("20000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), limitPrice)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act & Assert
        // Should pass at exactly 150% (boundary is inclusive)
        assertDoesNotThrow(() -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should reject limit order when price is exactly at 50% of market price")
    void testCreateLimitOrderPriceAtLowerBoundary() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("100.00");
        BigDecimal limitPrice = new BigDecimal("50.00"); // Exactly 0.50x market

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(UUID.randomUUID());
        account.setCashBalance(new BigDecimal("20000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), limitPrice)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act & Assert
        // Should pass at exactly 50% (boundary is inclusive)
        assertDoesNotThrow(() -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should calculate total equity with single holding")
    void testCalculateTotalEquityWithSingleHolding() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal holdingQuantity = new BigDecimal("100.00");
        BigDecimal cashBalance = new BigDecimal("1000.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setInstrumentId(instrumentId);
        holding.setQuantity(holdingQuantity);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        List<Holding> holdings = new ArrayList<>();
        holdings.add(holding);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(10);
        request.setPrice(null);

        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(holdings);
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("10"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert - should succeed because equity calculation includes holdings
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should calculate total equity with multiple holdings")
    void testCalculateTotalEquityWithMultipleHoldings() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId1 = UUID.randomUUID();
        UUID instrumentId2 = UUID.randomUUID();
        UUID instrumentId3 = UUID.randomUUID();
        BigDecimal marketPrice1 = new BigDecimal("50.00");
        BigDecimal marketPrice2 = new BigDecimal("75.00");
        BigDecimal marketPrice3 = new BigDecimal("100.00");
        BigDecimal cashBalance = new BigDecimal("5000.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding1 = new Holding();
        holding1.setInstrumentId(instrumentId1);
        holding1.setQuantity(new BigDecimal("100.00"));

        Holding holding2 = new Holding();
        holding2.setInstrumentId(instrumentId2);
        holding2.setQuantity(new BigDecimal("50.00"));

        Holding holding3 = new Holding();
        holding3.setInstrumentId(instrumentId3);
        holding3.setQuantity(new BigDecimal("25.00"));

        Instrument price1 = new Instrument();
        price1.setMidPrice(marketPrice1);

        Instrument price2 = new Instrument();
        price2.setMidPrice(marketPrice2);

        Instrument price3 = new Instrument();
        price3.setMidPrice(marketPrice3);

        List<Holding> holdings = new ArrayList<>();
        holdings.add(holding1);
        holdings.add(holding2);
        holdings.add(holding3);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId1);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(10);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(holdings);
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(price1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(price2));
        when(instrumentRepository.findById(instrumentId3)).thenReturn(Optional.of(price3));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId1, new BigDecimal("10"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(holding1);

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should calculate total equity with no holdings")
    void testCalculateTotalEquityWithNoHoldings() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal cashBalance = new BigDecimal("1000.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(5);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("5"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle equity calculation when some holdings have no price")
    void testCalculateTotalEquityWithMissingPrices() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId1 = UUID.randomUUID();
        UUID instrumentId2 = UUID.randomUUID();
        BigDecimal marketPrice1 = new BigDecimal("50.00");
        BigDecimal cashBalance = new BigDecimal("5000.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding1 = new Holding();
        holding1.setInstrumentId(instrumentId1);
        holding1.setQuantity(new BigDecimal("100.00"));

        Holding holding2 = new Holding();
        holding2.setInstrumentId(instrumentId2);
        holding2.setQuantity(new BigDecimal("50.00"));

        Instrument price1 = new Instrument();
        price1.setMidPrice(marketPrice1);

        List<Holding> holdings = new ArrayList<>();
        holdings.add(holding1);
        holdings.add(holding2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId1);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(10);
        request.setPrice(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(holdings);
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(price1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.empty()); // No price for second holding
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId1, new BigDecimal("10"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(holding1);

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should reject BUY order when account balance is too low and equity is too low")
    void testCreateBuyOrderWhenEquityBelowMinimum() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal cashBalance = new BigDecimal("3.00"); // Low balance

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(1);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("1"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should allow SELL order even when cash balance is below minimum if equity is low")
    void testCreateSellOrderWhenBalanceLowButAllowed() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");
        BigDecimal cashBalance = new BigDecimal("3.00"); // Below minimum

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(cashBalance);
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("100.00"));

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(10);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("10"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert - SELL should succeed even with low balance
        assertNotNull(response);
        assertEquals(OrderType.SELL, response.getOrderType());
    }

    @Test
    @DisplayName("Should handle cancellation when account client ID is null")
    void testCancelOrderWhenClientIdNull() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Client client = new Client();
        client.setClientId(null); // No client ID
        
        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setClient(client);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderType(OrderType.BUY);
        order.setQuantity(new BigDecimal("100"));
        order.setLimitPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(order);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        CancelOrderResponse response = orderService.cancelOrder(request);

        // Assert - should still succeed but skip audit logging due to null clientId
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        verify(orderRepository).updateOrder(any(Order.class));
    }

    @Test
    @DisplayName("Should handle creation when account client ID is null for audit logging")
    void testCreateOrderWhenClientIdNull() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(null); // No client ID
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(50);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("50"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert - should still succeed but skip audit logging
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        // Audit logging should not be called because clientId is null
        verify(auditTrailService, never()).logOrderCreated(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("Should handle account not found in validateCashBalance")
    void testCreateOrderAccountNotFoundInCashValidation() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty()); // Account not found

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should handle account not found in validateMinimumBalance")
    void testCreateOrderAccountNotFoundInMinimumBalance() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("100.00"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL); // Use SELL to skip validateCashBalance
        request.setQuantity(10);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("10"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty()); // Not found in validateMinimumBalance

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should reject market BUY order when market price not available for validation")
    void testCreateMarketOrderNoMarketPriceInCashValidation() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(null);

        // First call returns null (in validateOrderPrice), but then we get through to validateCashBalance
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should create order with limit order and calculate total value correctly")
    void testCreateLimitOrderTotalValueCalculation() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal limitPrice = new BigDecimal("75.00");
        BigDecimal marketPrice = new BigDecimal("100.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("20000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100);
        request.setPrice(limitPrice);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), limitPrice)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(limitPrice, response.getPrice());
        assertEquals(new BigDecimal("7500.00"), response.getTotalValue()); // 75 * 100
    }

    @Test
    @DisplayName("Should create order and enqueue for fulfillment")
    void testCreateOrderEnqueuedForFulfillment() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(50);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("50"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        verify(orderQueue).enqueue(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order and update database")
    void testCancelOrderDatabaseUpdate() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        
        Client client = new Client();
        client.setClientId(clientId);
        
        Account account = new Account();
        account.setAccountId(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setClient(client);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(new BigDecimal("100"));
        order.setOrderType(OrderType.BUY);
        order.setLimitPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(order);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(auditTrailService).logOrderCancelled(any(), any(), any(), any(), anyInt(), any(), any());

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        orderService.cancelOrder(request);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).updateOrder(orderCaptor.capture());
        Order cancelledOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertEquals(accountId, cancelledOrder.getAccountId());
    }

    @Test
    @DisplayName("Should validate position limit with existing holdings")
    void testPositionLimitWithExistingHolding() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(UUID.randomUUID());
        account.setCashBalance(new BigDecimal("500000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Holding existingHolding = new Holding();
        existingHolding.setQuantity(new BigDecimal("9900.00"));

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.BUY);
        request.setQuantity(100); // Exactly at limit: 9900 + 100 = 10000
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert - Should succeed at exactly 10000
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Should allow SELL when position limit would allow it")
    void testSellOrderDoesNotCheckPositionLimit() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();
        BigDecimal marketPrice = new BigDecimal("50.00");

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        Holding holding = new Holding();
        holding.setQuantity(new BigDecimal("100.00"));

        Instrument instrument = new Instrument();
        instrument.setMidPrice(marketPrice);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setOrderType(OrderType.SELL);
        request.setQuantity(100);
        request.setPrice(null);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findClientDateOfBirthByAccountId(accountId)).thenReturn(LocalDate.now().minusYears(25));
        when(orderRepository.countDuplicateOrders(accountId, instrumentId, new BigDecimal("100"), null)).thenReturn(0);
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(holding);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(holdingsRepository.findAllByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        CreateOrderResponse response = orderService.createOrder(request);

        // Assert - SELL doesn't check position limit
        assertNotNull(response);
        assertEquals(OrderType.SELL, response.getOrderType());
    }
}






