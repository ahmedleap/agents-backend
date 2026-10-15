package com.agentsbackend.services;

import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Order;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.repos.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@DisplayName("OrderRetryRoutingService Tests")
class OrderRetryRoutingServiceTest {

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    private OrderRetryRoutingService orderRetryRoutingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderRetryRoutingService = new OrderRetryRoutingService(
            kafkaTemplate,
            orderRepository,
            "order-fulfillment-retry",
            "order-fulfillment-dlt"
        );
    }

    @Test
    @DisplayName("Should set rejected status and persist before sending to dead-letter topic")
    void testSendToDeadLetterSetsRejectedStatus() {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        order.setAccount(account);

        orderRetryRoutingService.sendToDeadLetter(order, "retry exhausted");

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertNotNull(order.getCancelledAt());
        assertNotNull(order.getCancelReason());
        verify(orderRepository).updateOrder(eq(order));
        verify(kafkaTemplate).send(any(org.springframework.messaging.Message.class));
    }
}
