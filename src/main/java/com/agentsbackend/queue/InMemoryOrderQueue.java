package com.agentsbackend.queue;

import com.agentsbackend.entities.Order;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of OrderQueue.
 * Thread-safe using CopyOnWriteArrayList.
 * Later to be replaced with Kafka implementation.
 */
@Component
public class InMemoryOrderQueue implements OrderQueue {
    
    private final CopyOnWriteArrayList<Order> queue = new CopyOnWriteArrayList<>();
    
    @Override
    public void enqueue(Order order) {
        queue.add(order);
    }
    
    @Override
    public List<Order> getPendingOrders() {
        return new ArrayList<>(queue);
    }
    
    @Override
    public void remove(Order order) {
        queue.remove(order);
    }
    
    @Override
    public int size() {
        return queue.size();
    }
}
