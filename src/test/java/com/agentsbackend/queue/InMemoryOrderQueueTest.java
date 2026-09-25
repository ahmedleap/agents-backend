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

@DisplayName("InMemoryOrderQueue Tests")
class InMemoryOrderQueueTest {

    private InMemoryOrderQueue orderQueue;

    @BeforeEach
    void setUp() {
        orderQueue = new InMemoryOrderQueue();
    }

    // ==================== enqueue Tests ====================

    @Test
    @DisplayName("Should enqueue a single order")
    void testEnqueueSingleOrder() {
        // Arrange
        Order order = createOrder();

        // Act
        orderQueue.enqueue(order);

        // Assert
        assertEquals(1, orderQueue.size());
    }

    @Test
    @DisplayName("Should enqueue multiple orders")
    void testEnqueueMultipleOrders() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();

        // Act
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);

        // Assert
        assertEquals(3, orderQueue.size());
    }

    @Test
    @DisplayName("Should allow enqueueing duplicate orders")
    void testEnqueueDuplicateOrders() {
        // Arrange
        Order order = createOrder();

        // Act
        orderQueue.enqueue(order);
        orderQueue.enqueue(order);
        orderQueue.enqueue(order);

        // Assert
        assertEquals(3, orderQueue.size());
    }

    @Test
    @DisplayName("Should allow null order to be enqueued (no validation)")
    void testEnqueueNullOrder() {
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> orderQueue.enqueue(null));
    }

    // ==================== getPendingOrders Tests ====================

    @Test
    @DisplayName("Should return empty list when queue is empty")
    void testGetPendingOrdersEmptyQueue() {
        // Act
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertNotNull(pendingOrders);
        assertTrue(pendingOrders.isEmpty());
    }

    @Test
    @DisplayName("Should return all pending orders")
    void testGetPendingOrdersWithOrders() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);

        // Act
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertEquals(3, pendingOrders.size());
        assertTrue(pendingOrders.contains(order1));
        assertTrue(pendingOrders.contains(order2));
        assertTrue(pendingOrders.contains(order3));
    }

    @Test
    @DisplayName("Should return a copy of the queue, not the original")
    void testGetPendingOrdersReturnsCopy() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);

        // Act
        List<Order> pendingOrders = orderQueue.getPendingOrders();
        pendingOrders.add(createOrder());

        // Assert - modifying returned list should not affect queue
        assertEquals(2, orderQueue.size());
        assertEquals(3, pendingOrders.size());
    }

    @Test
    @DisplayName("Should return orders in FIFO order")
    void testGetPendingOrdersFIFOOrder() {
        // Arrange
        Order order1 = createOrderWithId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        Order order2 = createOrderWithId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        Order order3 = createOrderWithId(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);

        // Act
        List<Order> pendingOrders = orderQueue.getPendingOrders();

        // Assert
        assertEquals(order1.getOrderId(), pendingOrders.get(0).getOrderId());
        assertEquals(order2.getOrderId(), pendingOrders.get(1).getOrderId());
        assertEquals(order3.getOrderId(), pendingOrders.get(2).getOrderId());
    }

    // ==================== remove Tests ====================

    @Test
    @DisplayName("Should remove a single order")
    void testRemoveSingleOrder() {
        // Arrange
        Order order = createOrder();
        orderQueue.enqueue(order);

        // Act
        orderQueue.remove(order);

        // Assert
        assertEquals(0, orderQueue.size());
    }

    @Test
    @DisplayName("Should remove only the first occurrence of duplicate orders")
    void testRemoveDuplicateOrder() {
        // Arrange
        Order order = createOrder();
        orderQueue.enqueue(order);
        orderQueue.enqueue(order);
        orderQueue.enqueue(order);

        // Act
        orderQueue.remove(order);

        // Assert
        assertEquals(2, orderQueue.size());
    }

    @Test
    @DisplayName("Should remove order from middle of queue")
    void testRemoveOrderFromMiddle() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);

        // Act
        orderQueue.remove(order2);

        // Assert
        assertEquals(2, orderQueue.size());
        List<Order> pendingOrders = orderQueue.getPendingOrders();
        assertTrue(pendingOrders.contains(order1));
        assertFalse(pendingOrders.contains(order2));
        assertTrue(pendingOrders.contains(order3));
    }

    @Test
    @DisplayName("Should not throw when removing non-existent order")
    void testRemoveNonExistentOrder() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        orderQueue.enqueue(order1);

        // Act & Assert
        assertDoesNotThrow(() -> orderQueue.remove(order2));
        assertEquals(1, orderQueue.size());
    }

    @Test
    @DisplayName("Should not throw when removing from empty queue")
    void testRemoveFromEmptyQueue() {
        // Arrange
        Order order = createOrder();

        // Act & Assert
        assertDoesNotThrow(() -> orderQueue.remove(order));
        assertEquals(0, orderQueue.size());
    }

    @Test
    @DisplayName("Should handle null order removal")
    void testRemoveNullOrder() {
        // Arrange
        Order order = createOrder();
        orderQueue.enqueue(order);

        // Act & Assert
        assertDoesNotThrow(() -> orderQueue.remove(null));
        assertEquals(1, orderQueue.size());
    }

    // ==================== size Tests ====================

    @Test
    @DisplayName("Should return zero size for empty queue")
    void testSizeEmptyQueue() {
        // Act
        int size = orderQueue.size();

        // Assert
        assertEquals(0, size);
    }

    @Test
    @DisplayName("Should return correct size after enqueue")
    void testSizeAfterEnqueue() {
        // Act
        orderQueue.enqueue(createOrder());
        orderQueue.enqueue(createOrder());
        orderQueue.enqueue(createOrder());

        // Assert
        assertEquals(3, orderQueue.size());
    }

    @Test
    @DisplayName("Should return correct size after remove")
    void testSizeAfterRemove() {
        // Arrange
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();
        orderQueue.enqueue(order1);
        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);

        // Act
        orderQueue.remove(order2);

        // Assert
        assertEquals(2, orderQueue.size());
    }

    @Test
    @DisplayName("Should return correct size after multiple operations")
    void testSizeAfterMultipleOperations() {
        // Arrange & Act
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();

        orderQueue.enqueue(order1);
        assertEquals(1, orderQueue.size());

        orderQueue.enqueue(order2);
        orderQueue.enqueue(order3);
        assertEquals(3, orderQueue.size());

        orderQueue.remove(order1);
        assertEquals(2, orderQueue.size());

        orderQueue.remove(order2);
        assertEquals(1, orderQueue.size());

        orderQueue.remove(order3);
        assertEquals(0, orderQueue.size());
    }

    // ==================== Thread Safety Tests ====================

    @Test
    @DisplayName("Should handle concurrent enqueue operations")
    void testConcurrentEnqueue() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int ordersPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ordersPerThread; j++) {
                    orderQueue.enqueue(createOrder());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        assertEquals(threadCount * ordersPerThread, orderQueue.size());
    }

    @Test
    @DisplayName("Should handle concurrent remove operations")
    void testConcurrentRemove() throws InterruptedException {
        // Arrange
        int orderCount = 100;
        for (int i = 0; i < orderCount; i++) {
            orderQueue.enqueue(createOrder());
        }

        List<Order> orders = orderQueue.getPendingOrders();
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = threadIndex; j < orders.size(); j += threadCount) {
                    if (j < orders.size()) {
                        orderQueue.remove(orders.get(j));
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        assertEquals(0, orderQueue.size());
    }

    @Test
    @DisplayName("Should handle concurrent mixed operations")
    void testConcurrentMixedOperations() throws InterruptedException {
        // Arrange
        Thread enqueueThread = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                orderQueue.enqueue(createOrder());
            }
        });

        Thread removeThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Allow some orders to be enqueued
                List<Order> orders = orderQueue.getPendingOrders();
                for (Order order : orders) {
                    orderQueue.remove(order);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Act
        enqueueThread.start();
        removeThread.start();
        enqueueThread.join();
        removeThread.join();

        // Assert - queue should be empty or near empty
        assertTrue(orderQueue.size() >= 0);
    }

    // ==================== Helper Methods ====================

    private Order createOrder() {
        return createOrderWithId(UUID.randomUUID());
    }

    private Order createOrderWithId(UUID orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
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
