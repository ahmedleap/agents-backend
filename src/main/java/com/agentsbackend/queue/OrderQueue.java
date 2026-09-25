package com.agentsbackend.queue;

import com.agentsbackend.entities.Order;
import java.util.List;

/**
 * Abstraction for order queue - allows swapping in-memory queue for Kafka later.
 */
public interface OrderQueue {
    
    /**
     * Add an order to the queue for fulfillment processing.
     */
    void enqueue(Order order);
    
    /**
     * Get all pending orders from the queue.
     */
    List<Order> getPendingOrders();
    
    /**
     * Remove an order from the queue (after it's been processed).
     */
    void remove(Order order);
    
    /**
     * Get the size of the queue.
     */
    int size();
}
