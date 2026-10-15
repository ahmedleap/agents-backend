package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.repos.transactional.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Publishes unfilled limit orders to retry and DLQ topics.
 */
@Service
public class OrderRetryRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(OrderRetryRoutingService.class);

    private final KafkaTemplate<String, Order> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final String retryTopic;
    private final String deadLetterTopic;

    public OrderRetryRoutingService(
        KafkaTemplate<String, Order> kafkaTemplate,
        OrderRepository orderRepository,
        @Value("${kafka.topic.retry:order-fulfillment-retry}") String retryTopic,
        @Value("${kafka.topic.dlq:order-fulfillment-dlt}") String deadLetterTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
        this.retryTopic = retryTopic;
        this.deadLetterTopic = deadLetterTopic;
    }

    public void sendToRetry(Order order, String reason) {
        String key = order.getAccount().getAccountId().toString();
        kafkaTemplate.send(
            MessageBuilder.withPayload(order)
                .setHeader(KafkaHeaders.TOPIC, retryTopic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("x-retry-reason", reason)
                .build()
        );

        logger.info("Order {} routed to retry topic {}", order.getOrderId(), retryTopic);
    }

    public void sendToDeadLetter(Order order, String reason) {
        // Persist order state as REJECTED when routed to DLT.
        order.setStatus(OrderStatus.REJECTED);
        order.setCancelReason("Moved to dead-letter topic: " + reason);
        order.setCancelledAt(OffsetDateTime.now(ZoneOffset.UTC));
        orderRepository.updateOrder(order);

        String key = order.getAccount().getAccountId().toString();
        kafkaTemplate.send(
            MessageBuilder.withPayload(order)
                .setHeader(KafkaHeaders.TOPIC, deadLetterTopic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("x-dlq-reason", reason)
                .build()
        );

        logger.info("Order {} routed to dead-letter topic {}", order.getOrderId(), deadLetterTopic);
    }
}
