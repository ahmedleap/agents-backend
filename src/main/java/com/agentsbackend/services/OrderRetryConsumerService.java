package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Consumes orders from the retry topic.
 * If still not fillable, routes order to DLQ.
 */
@Service
@ConditionalOnProperty(
    name = "order.processing.mode",
    havingValue = "kafka"
)
public class OrderRetryConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderRetryConsumerService.class);

    private final OrderConsumerService orderConsumerService;
    private final OrderRetryRoutingService orderRetryRoutingService;
    private final TaskScheduler taskScheduler;
    private final long retryDelayMs;

    public OrderRetryConsumerService(
        OrderConsumerService orderConsumerService,
        OrderRetryRoutingService orderRetryRoutingService,
        TaskScheduler taskScheduler,
        @Value("${kafka.retry.delay-ms:60000}") long retryDelayMs
    ) {
        this.orderConsumerService = orderConsumerService;
        this.orderRetryRoutingService = orderRetryRoutingService;
        this.taskScheduler = taskScheduler;
        this.retryDelayMs = retryDelayMs;
    }

    @KafkaListener(
        topics = "${kafka.topic.retry:order-fulfillment-retry}",
        groupId = "order-fulfillment-retry-group"
    )
    public void processRetryOrder(Order order) {
        try {
            logger.info("Received order {} from retry topic", order.getOrderId());

            if (orderConsumerService.isCancelled(order)) {
                logger.info("Skipping cancelled order {} from retry topic", order.getOrderId());
                return;
            }

            if (order.getLimitPrice() == null) {
                logger.warn("Order {} on retry topic is not a limit order; routing to DLQ", order.getOrderId());
                orderRetryRoutingService.sendToDeadLetter(order, "Non-limit order received on retry topic");
                return;
            }

            Instant executeAt = Instant.now().plusMillis(retryDelayMs);
            taskScheduler.schedule(() -> processDelayedRetry(order), executeAt);
            logger.info("Scheduled retry execution for order {} at {}", order.getOrderId(), executeAt);
        } catch (Exception e) {
            logger.error("Error processing retry order {}: {}", order.getOrderId(), e.getMessage(), e);
            orderRetryRoutingService.sendToDeadLetter(order, "Exception while processing retry order");
        }
    }

    private void processDelayedRetry(Order order) {
        try {
            if (orderConsumerService.isCancelled(order)) {
                logger.info("Skipping cancelled order {} during delayed retry", order.getOrderId());
                return;
            }
            boolean filled = orderConsumerService.attemptFill(order);
            if (!filled) {
                orderRetryRoutingService.sendToDeadLetter(order, "Limit price was not reached after scheduled retry attempt");
            }
        } catch (Exception e) {
            logger.error("Scheduled retry execution failed for order {}: {}", order.getOrderId(), e.getMessage(), e);
            orderRetryRoutingService.sendToDeadLetter(order, "Exception while executing scheduled retry");
        }
    }
}
