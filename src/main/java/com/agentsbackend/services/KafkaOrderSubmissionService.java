package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order submission service using Kafka.
 * Active when order.processing.mode=kafka.
 */
@Service
@ConditionalOnProperty(
    name = "order.processing.mode",
    havingValue = "kafka"
)
public class KafkaOrderSubmissionService implements OrderSubmissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaOrderSubmissionService.class);
    private static final String ORDERS_TOPIC = "order-fulfillment";
    
    private final KafkaTemplate<String, Order> kafkaTemplate;
    
    public KafkaOrderSubmissionService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public void submitOrder(Order order) {
        try {
            // Use accountId as partition key to ensure orders from same account are processed in order
            String partitionKey = order.getAccount().getAccountId().toString();
            kafkaTemplate.send(ORDERS_TOPIC, partitionKey, order);
            logger.info("Order {} published to Kafka topic: {} with partition key: {}", 
                order.getOrderId(), ORDERS_TOPIC, partitionKey);
        } catch (Exception e) {
            logger.error("Failed to publish order {} to Kafka: {}", order.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to submit order for fulfillment", e);
        }
    }
}
