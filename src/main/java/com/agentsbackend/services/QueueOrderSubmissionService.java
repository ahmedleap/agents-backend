package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.queue.OrderQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order submission service using in-memory queue.
 * Active when order.processing.mode=queue (default).
 */
@Service
@ConditionalOnProperty(
    name = "order.processing.mode",
    havingValue = "queue",
    matchIfMissing = true
)
public class QueueOrderSubmissionService implements OrderSubmissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueOrderSubmissionService.class);
    
    private final OrderQueue orderQueue;
    
    public QueueOrderSubmissionService(OrderQueue orderQueue) {
        this.orderQueue = orderQueue;
    }
    
    @Override
    public void submitOrder(Order order) {
        try {
            orderQueue.enqueue(order);
            logger.info("Order {} added to in-memory queue", order.getOrderId());
        } catch (Exception e) {
            logger.error("Failed to add order {} to queue: {}", order.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to submit order to queue", e);
        }
    }
}
