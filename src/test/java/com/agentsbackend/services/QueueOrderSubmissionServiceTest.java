package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.queue.OrderQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("QueueOrderSubmissionService Tests")
class QueueOrderSubmissionServiceTest {

    @Mock
    private OrderQueue orderQueue;

    private QueueOrderSubmissionService queueOrderSubmissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueOrderSubmissionService = new QueueOrderSubmissionService(orderQueue);
    }

    @Test
    @DisplayName("Should enqueue order to in-memory queue")
    void testSubmitOrderEnqueuesOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setOrderType(OrderType.BUY);
        order.setQuantity(new BigDecimal("100"));

        doNothing().when(orderQueue).enqueue(any(Order.class));

        // Act
        queueOrderSubmissionService.submitOrder(order);

        // Assert
        verify(orderQueue).enqueue(order);
    }

    @Test
    @DisplayName("Should throw exception when queue enqueue fails")
    void testSubmitOrderThrowsExceptionOnQueueFailure() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);

        doThrow(new RuntimeException("Queue overflow"))
            .when(orderQueue).enqueue(any(Order.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> queueOrderSubmissionService.submitOrder(order));
    }

    @Test
    @DisplayName("Should enqueue multiple orders sequentially")
    void testSubmitMultipleOrders() {
        // Arrange
        Order order1 = createTestOrder(UUID.randomUUID());
        Order order2 = createTestOrder(UUID.randomUUID());
        Order order3 = createTestOrder(UUID.randomUUID());

        doNothing().when(orderQueue).enqueue(any(Order.class));

        // Act
        queueOrderSubmissionService.submitOrder(order1);
        queueOrderSubmissionService.submitOrder(order2);
        queueOrderSubmissionService.submitOrder(order3);

        // Assert
        verify(orderQueue, times(3)).enqueue(any(Order.class));
        verify(orderQueue).enqueue(order1);
        verify(orderQueue).enqueue(order2);
        verify(orderQueue).enqueue(order3);
    }

    // Helper method
    private Order createTestOrder(UUID orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        order.setAccount(account);
        
        order.setOrderType(OrderType.BUY);
        order.setQuantity(new BigDecimal("100"));
        
        return order;
    }
}
