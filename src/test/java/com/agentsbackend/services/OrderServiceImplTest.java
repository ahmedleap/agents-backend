package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.InstrumentPrice;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
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
import com.agentsbackend.repos.InstrumentPriceRepository;
import com.agentsbackend.queue.OrderQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentPriceRepository instrumentPriceRepository;

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
            instrumentPriceRepository,
            orderQueue,
            auditTrailService
        );
    }

    @Test
    @DisplayName("Should retrieve all pending orders when no account filter provided")
    void testGetPendingOrdersWithoutFilter() {
        // Arrange
        List<Order> pendingOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID());
        order1.setStatus(OrderStatus.PENDING);
        pendingOrders.add(order1);

        when(orderRepository.findPendingOrders()).thenReturn(pendingOrders);

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        verify(orderRepository).findPendingOrders();
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
        when(orderRepository.findPendingOrders()).thenReturn(new ArrayList<>());

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findPendingOrders();
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void testCancelOrderSuccess() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(order);
        doNothing().when(orderRepository).updateOrder(any(Order.class));

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
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
        when(orderRepository.findPendingOrders()).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderService.getPendingOrders(request);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findPendingOrders();
        verify(orderRepository, never()).findPendingOrdersByAccount(any());
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
        List<Order> pendingOrders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderId(UUID.randomUUID());
            order.setStatus(OrderStatus.PENDING);
            pendingOrders.add(order);
        }

        when(orderRepository.findPendingOrders()).thenReturn(pendingOrders);

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
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
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(order);
        doNothing().when(orderRepository).updateOrder(any(Order.class));

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        orderService.cancelOrder(request);

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
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

        when(accountRepository.findById(accountId)).thenReturn(account);
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
        when(accountRepository.findById(accountId)).thenReturn(null);

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

        when(accountRepository.findById(accountId)).thenReturn(account);
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

        when(accountRepository.findById(accountId)).thenReturn(account);
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
}
