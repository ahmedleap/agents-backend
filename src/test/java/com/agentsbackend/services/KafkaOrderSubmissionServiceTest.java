package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("KafkaOrderSubmissionService Tests")
class KafkaOrderSubmissionServiceTest {

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    private KafkaOrderSubmissionService kafkaOrderSubmissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kafkaOrderSubmissionService = new KafkaOrderSubmissionService(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish order to Kafka with accountId as partition key")
    void testSubmitOrderPublishesToKafka() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setOrderType(OrderType.BUY);
        order.setQuantity(new BigDecimal("100"));

        when(kafkaTemplate.send(anyString(), anyString(), any(Order.class)))
            .thenReturn(null);

        // Act
        kafkaOrderSubmissionService.submitOrder(order);

        // Assert
        verify(kafkaTemplate).send(
            eq("order-fulfillment"),
            eq(accountId.toString()),
            eq(order)
        );
    }

    @Test
    @DisplayName("Should throw exception when Kafka publish fails")
    void testSubmitOrderThrowsExceptionOnKafkaFailure() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);

        when(kafkaTemplate.send(anyString(), anyString(), any(Order.class)))
            .thenThrow(new RuntimeException("Kafka connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kafkaOrderSubmissionService.submitOrder(order));
    }

    @Test
    @DisplayName("Should use order-fulfillment topic for all orders")
    void testOrdersPublishedToCorrectTopic() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setAccountId(accountId);
        
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(account);

        when(kafkaTemplate.send(anyString(), anyString(), any(Order.class)))
            .thenReturn(null);

        // Act
        kafkaOrderSubmissionService.submitOrder(order);

        // Assert - verify topic name
        verify(kafkaTemplate).send(
            eq("order-fulfillment"),
            anyString(),
            any(Order.class)
        );
    }

    @Test
    @DisplayName("Should use different partition keys for different accounts")
    void testDifferentAccountsUseDifferentPartitionKeys() {
        // Arrange
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        
        Account account1 = new Account();
        account1.setAccountId(accountId1);
        
        Account account2 = new Account();
        account2.setAccountId(accountId2);
        
        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID());
        order1.setAccount(account1);
        
        Order order2 = new Order();
        order2.setOrderId(UUID.randomUUID());
        order2.setAccount(account2);

        when(kafkaTemplate.send(anyString(), anyString(), any(Order.class)))
            .thenReturn(null);

        // Act
        kafkaOrderSubmissionService.submitOrder(order1);
        kafkaOrderSubmissionService.submitOrder(order2);

        // Assert
        verify(kafkaTemplate).send(eq("order-fulfillment"), eq(accountId1.toString()), eq(order1));
        verify(kafkaTemplate).send(eq("order-fulfillment"), eq(accountId2.toString()), eq(order2));
    }
}
