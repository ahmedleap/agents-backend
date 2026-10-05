package com.agentsbackend.services;

import com.agentsbackend.entities.Order;

/**
 * Interface for order submission strategies.
 * Implementations can choose to process orders via queue or Kafka.
 */
public interface OrderSubmissionService {
    /**
     * Submit an order for processing.
     * Implementation depends on the active strategy (queue or Kafka).
     */
    void submitOrder(Order order);
}
