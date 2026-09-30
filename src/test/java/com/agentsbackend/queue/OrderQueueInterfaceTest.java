package com.agentsbackend.queue;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderQueue Interface Contract Tests")
class OrderQueueInterfaceTest {

    private OrderQueue orderQueue;

    @BeforeEach
    void setUp() {
        // Use the InMemoryOrderQueue implementation
        orderQueue = new InMemoryOrderQueue();
    }

    @Test
    @DisplayName("Implementation should satisfy enqueue contract")
    void testEnqueueContract() {
        // Arrange
        Order order = createOrder();

        // Act
        orderQueue.enqueue(order);

        // Assert
        assertEquals(1, orderQueue.size());
        assertTrue(orderQueue.getPendingOrders().contains(order));
    }

    @Test
    @DisplayName("Implementation should satisfy getPendingOrders contract")
    void testGetPendingOrdersContract() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();

        // Act
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertNotNull(pendingOrders);
        assertEquals(2, pendingOrders.size());
        assertTrue(pendingOrders.contains(order1));
        assertTrue(pendingOrders.contains(order2));
    }

    @Test
    @DisplayName("Implementation should satisfy remove contract")
    void testRemoveContract() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);

        // Act
        orderQueue.remove(order1);

        // Assert
        assertEquals(1, orderQueue.size());
        assertFalse(orderQueue.getPendingOrders().contains(order1));
        assertTrue(orderQueue.getPendingOrders().contains(order2));
    }

    @Test
    @DisplayName("Implementation should satisfy size contract")
    void testSizeContract() {
        // Act & Assert
        assertEquals(0, orderQueue.size());

        orderQueue.enqueue(createOrder());
        assertEquals(1, orderQueue.size());

        orderQueue.enqueue(createOrder());
        assertEquals(2, orderQueue.size());

        Order order = createOrder();
        orderQueue.enqueue(order);
        assertEquals(3, orderQueue.size());

        orderQueue.remove(order);
        assertEquals(2, orderQueue.size());
    }

    @Test
    @DisplayName("Enqueue followed by getPendingOrders should contain the order")
    void testEnqueueThenGetPendingOrders() {
        // Arrange
        Order order = createOrder();

        // Act
        orderQueue.enqueue(order);
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertTrue(pendingOrders.contains(order));
    }

    @Test
    @DisplayName("Remove followed by getPendingOrders should not contain the order")
    void testRemoveThenGetPendingOrders() {
        // Arrange
        Order order = createOrder();
        orderQueue.enqueue(order);

        // Act
        orderQueue.remove(order);
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertFalse(pendingOrders.contains(order));
    }

    @Test
    @DisplayName("Size should match getPendingOrders length")
    void testSizeMatchesGetPendingOrdersLength() {
        // Act
        orderQueue.enqueue(createOrder());
        orderQueue.enqueue(createOrder());
        orderQueue.enqueue(createOrder());

        // Assert
        assertEquals(orderQueue.size(), orderQueue.getPendingOrders().size());
    }

    @Test
    @DisplayName("Multiple enqueues should all be retrievable")
    void testMultipleEnqueuesRetrievable() {
        // Arrange
        Order[] orders = new Order[5];
        for (int i = 0; i < 5; i++) {
            orders[i] = createOrder();
        }

        // Act
        for (Order order : orders) {
            orderQueue.enqueue(order);
        }
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        for (Order order : orders) {
            assertTrue(pendingOrders.contains(order));
        }
    }

    @Test
    @DisplayName("Remove all orders should result in empty queue")
    void testRemoveAllOrders() {
        // Arrange
        Order[] orders = new Order[5];
        for (int i = 0; i < 5; i++) {
            orders[i] = createOrder();
            orderQueue.enqueue(orders[i]);
        }

        // Act
        for (Order order : orders) {
            orderQueue.remove(order);
        }

        // Assert
        assertEquals(0, orderQueue.size());
        assertTrue(orderQueue.getPendingOrders().isEmpty());
    }

    @Test
    @DisplayName("Queue should handle order lifecycle: enqueue -> get -> remove")
    void testOrderLifecycle() {
        // Arrange
        Order order = createOrder();

        // Act & Assert - enqueue
        orderQueue.enqueue(order);
        assertEquals(1, orderQueue.size());
        assertTrue(orderQueue.getPendingOrders().contains(order));

        // Act & Assert - get
        List<Order> pendingOrders = orderQueue.getPendingOrders();
        assertTrue(pendingOrders.contains(order));

        // Act & Assert - remove
        orderQueue.remove(order);
        assertEquals(0, orderQueue.size());
        assertFalse(orderQueue.getPendingOrders().contains(order));
    }

    // ==================== Helper Methods ====================

    private Order createOrder() {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setOrderType(OrderType.BUY);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(new BigDecimal("100.00"));
        order.setLimitPrice(new BigDecimal("50.00"));

        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        order.setAccount(account);

        Instrument instrument = new Instrument();
        instrument.setInstrumentId(UUID.randomUUID());
        order.setInstrument(instrument);

        order.setCreatedAt(LocalDateTime.now());

        return order;
    }
}
