package com.agentsbackend.repos;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    @Mock
    private OrderRepository orderRepository;

    private Order testOrder;
    private UUID testAccountId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAccountId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        
        testOrder = new Order();
        testOrder.setOrderId(testOrderId);
        testOrder.setOrderType(OrderType.BUY);
        testOrder.setQuantity(new BigDecimal("100.00"));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save order successfully")
    void testSaveOrder() {
        // Arrange
        doNothing().when(orderRepository).save(any(Order.class));

        // Act
        orderRepository.save(testOrder);

        // Assert
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should find order by id")
    void testFindById() {
        // Arrange
        when(orderRepository.findById(testOrderId)).thenReturn(testOrder);

        // Act
        Order retrievedOrder = orderRepository.findById(testOrderId);

        // Assert
        assertNotNull(retrievedOrder);
        assertEquals(testOrderId, retrievedOrder.getOrderId());
        assertEquals(OrderStatus.PENDING, retrievedOrder.getStatus());
        verify(orderRepository).findById(testOrderId);
    }

    @Test
    @DisplayName("Should return null when order not found")
    void testFindByIdNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentId)).thenReturn(null);

        // Act
        Order retrievedOrder = orderRepository.findById(nonExistentId);

        // Assert
        assertNull(retrievedOrder);
        verify(orderRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should find pending orders")
    void testFindPendingOrders() {
        // Arrange
        List<Order> pendingOrders = new ArrayList<>();
        pendingOrders.add(testOrder);
        
        Order order2 = new Order();
        order2.setOrderId(UUID.randomUUID());
        order2.setOrderType(OrderType.SELL);
        order2.setQuantity(new BigDecimal("50.00"));
        order2.setStatus(OrderStatus.PENDING);
        order2.setCreatedAt(LocalDateTime.now());
        pendingOrders.add(order2);

        when(orderRepository.findPendingOrders()).thenReturn(pendingOrders);

        // Act
        List<Order> result = orderRepository.findPendingOrders();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getStatus() == OrderStatus.PENDING));
        verify(orderRepository).findPendingOrders();
    }

    @Test
    @DisplayName("Should find pending orders by account")
    void testFindPendingOrdersByAccount() {
        // Arrange
        List<Order> accountOrders = new ArrayList<>();
        accountOrders.add(testOrder);

        when(orderRepository.findPendingOrdersByAccount(testAccountId)).thenReturn(accountOrders);

        // Act
        List<Order> result = orderRepository.findPendingOrdersByAccount(testAccountId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        verify(orderRepository).findPendingOrdersByAccount(testAccountId);
    }

    @Test
    @DisplayName("Should find all orders by account")
    void testFindAllByAccount() {
        // Arrange
        List<Order> accountOrders = new ArrayList<>();
        accountOrders.add(testOrder);
        
        Order filledOrder = new Order();
        filledOrder.setOrderId(UUID.randomUUID());
        filledOrder.setOrderType(OrderType.BUY);
        filledOrder.setQuantity(new BigDecimal("50.00"));
        filledOrder.setStatus(OrderStatus.FILLED);
        filledOrder.setCreatedAt(LocalDateTime.now());
        accountOrders.add(filledOrder);

        when(orderRepository.findAllByAccount(testAccountId)).thenReturn(accountOrders);

        // Act
        List<Order> result = orderRepository.findAllByAccount(testAccountId);

        // Assert
        assertEquals(2, result.size());
        verify(orderRepository).findAllByAccount(testAccountId);
    }

    @Test
    @DisplayName("Should update order status")
    void testUpdateOrder() {
        // Arrange
        testOrder.setStatus(OrderStatus.FILLED);
        testOrder.setFilledPrice(new BigDecimal("50.00"));
        testOrder.setFilledAt(LocalDateTime.now());
        
        doNothing().when(orderRepository).updateOrder(any(Order.class));

        // Act
        orderRepository.updateOrder(testOrder);

        // Assert
        verify(orderRepository).updateOrder(testOrder);
    }

    @Test
    @DisplayName("Should count duplicate orders")
    void testCountDuplicateOrders() {
        // Arrange
        UUID instrumentId = UUID.randomUUID();
        when(orderRepository.countDuplicateOrders(
            testAccountId, 
            instrumentId, 
            new BigDecimal("100.00"), 
            new BigDecimal("50.00")
        )).thenReturn(1);

        // Act
        int count = orderRepository.countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("100.00"),
            new BigDecimal("50.00")
        );

        // Assert
        assertEquals(1, count);
        verify(orderRepository).countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("100.00"),
            new BigDecimal("50.00")
        );
    }

    @Test
    @DisplayName("Should return zero duplicate orders when none exist")
    void testCountDuplicateOrdersNoMatches() {
        // Arrange
        UUID instrumentId = UUID.randomUUID();
        when(orderRepository.countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("100.00"),
            new BigDecimal("50.00")
        )).thenReturn(0);

        // Act
        int count = orderRepository.countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("100.00"),
            new BigDecimal("50.00")
        );

        // Assert
        assertEquals(0, count);
        verify(orderRepository).countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("100.00"),
            new BigDecimal("50.00")
        );
    }

    @Test
    @DisplayName("Should return empty when no pending orders for account")
    void testFindPendingOrdersByAccountEmpty() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(orderRepository.findPendingOrdersByAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderRepository.findPendingOrdersByAccount(accountId);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository).findPendingOrdersByAccount(accountId);
    }

    @Test
    @DisplayName("Should return empty list when no pending orders exist")
    void testFindPendingOrdersEmpty() {
        // Arrange
        when(orderRepository.findPendingOrders()).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderRepository.findPendingOrders();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findPendingOrders();
    }

    @Test
    @DisplayName("Should update order with cancelled status")
    void testUpdateOrderCancelled() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        testOrder.setCancelledAt(LocalDateTime.now());
        
        doNothing().when(orderRepository).updateOrder(any(Order.class));

        // Act
        orderRepository.updateOrder(testOrder);

        // Assert
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        assertNotNull(testOrder.getCancelledAt());
        verify(orderRepository).updateOrder(testOrder);
    }

    @Test
    @DisplayName("Should handle BUY order types")
    void testBuyOrderType() {
        // Arrange
        Order buyOrder = new Order();
        buyOrder.setOrderId(UUID.randomUUID());
        buyOrder.setOrderType(OrderType.BUY);
        buyOrder.setQuantity(new BigDecimal("100.00"));
        buyOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(buyOrder.getOrderId())).thenReturn(buyOrder);

        // Act
        Order result = orderRepository.findById(buyOrder.getOrderId());

        // Assert
        assertEquals(OrderType.BUY, result.getOrderType());
    }

    @Test
    @DisplayName("Should handle SELL order types")
    void testSellOrderType() {
        // Arrange
        Order sellOrder = new Order();
        sellOrder.setOrderId(UUID.randomUUID());
        sellOrder.setOrderType(OrderType.SELL);
        sellOrder.setQuantity(new BigDecimal("50.00"));
        sellOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(sellOrder.getOrderId())).thenReturn(sellOrder);

        // Act
        Order result = orderRepository.findById(sellOrder.getOrderId());

        // Assert
        assertEquals(OrderType.SELL, result.getOrderType());
    }

    @Test
    @DisplayName("Should handle multiple duplicate orders")
    void testCountMultipleDuplicateOrders() {
        // Arrange
        UUID instrumentId = UUID.randomUUID();
        when(orderRepository.countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("50.00"),
            new BigDecimal("25.00")
        )).thenReturn(5);

        // Act
        int count = orderRepository.countDuplicateOrders(
            testAccountId,
            instrumentId,
            new BigDecimal("50.00"),
            new BigDecimal("25.00")
        );

        // Assert
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should find all orders by account when multiple statuses exist")
    void testFindAllByAccountMultipleStatuses() {
        // Arrange
        List<Order> accountOrders = new ArrayList<>();
        
        Order pendingOrder = new Order();
        pendingOrder.setOrderId(UUID.randomUUID());
        pendingOrder.setStatus(OrderStatus.PENDING);
        accountOrders.add(pendingOrder);
        
        Order filledOrder = new Order();
        filledOrder.setOrderId(UUID.randomUUID());
        filledOrder.setStatus(OrderStatus.FILLED);
        accountOrders.add(filledOrder);
        
        Order cancelledOrder = new Order();
        cancelledOrder.setOrderId(UUID.randomUUID());
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        accountOrders.add(cancelledOrder);

        when(orderRepository.findAllByAccount(testAccountId)).thenReturn(accountOrders);

        // Act
        List<Order> result = orderRepository.findAllByAccount(testAccountId);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(o -> o.getStatus() == OrderStatus.PENDING));
        assertTrue(result.stream().anyMatch(o -> o.getStatus() == OrderStatus.FILLED));
        assertTrue(result.stream().anyMatch(o -> o.getStatus() == OrderStatus.CANCELLED));
    }
}
