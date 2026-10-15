package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OrderRetryConsumerService Tests")
class OrderRetryConsumerServiceTest {

    @Mock
    private OrderConsumerService orderConsumerService;

    @Mock
    private OrderRetryRoutingService orderRetryRoutingService;

    @Mock
    private TaskScheduler taskScheduler;

    private OrderRetryConsumerService orderRetryConsumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderRetryConsumerService = new OrderRetryConsumerService(
            orderConsumerService,
            orderRetryRoutingService,
            taskScheduler,
            60000
        );
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(null);
    }

    @Test
    @DisplayName("Should schedule retry execution for limit order")
    void testSchedulesRetryExecutionForLimitOrder() {
        Order order = createLimitOrder();

        orderRetryConsumerService.processRetryOrder(order);

        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("Should send unfilled scheduled retry order to DLQ")
    void testSendToDlqWhenScheduledRetryStillUnfilled() {
        Order order = createLimitOrder();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(taskScheduler.schedule(runnableCaptor.capture(), any(Instant.class))).thenReturn(null);
        when(orderConsumerService.attemptFill(order)).thenReturn(true);

        orderRetryConsumerService.processRetryOrder(order);

        when(orderConsumerService.attemptFill(order)).thenReturn(false);
        runnableCaptor.getValue().run();

        verify(orderRetryRoutingService).sendToDeadLetter(eq(order), anyString());
    }

    @Test
    @DisplayName("Should not send to DLQ when scheduled retry fill succeeds")
    void testNoDlqWhenScheduledRetryFilled() {
        Order order = createLimitOrder();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(taskScheduler.schedule(runnableCaptor.capture(), any(Instant.class))).thenReturn(null);
        when(orderConsumerService.attemptFill(order)).thenReturn(true);

        orderRetryConsumerService.processRetryOrder(order);
        runnableCaptor.getValue().run();

        verify(orderRetryRoutingService, never()).sendToDeadLetter(eq(order), anyString());
    }

    @Test
    @DisplayName("Should route market order on retry topic to DLQ")
    void testMarketOrderOnRetryTopicGoesToDlq() {
        Order order = createLimitOrder();
        order.setLimitPrice(null);

        assertDoesNotThrow(() -> orderRetryConsumerService.processRetryOrder(order));

        verify(orderRetryRoutingService).sendToDeadLetter(eq(order), anyString());
        verify(orderConsumerService, never()).attemptFill(order);
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    private Order createLimitOrder() {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setOrderType(OrderType.BUY);
        order.setLimitPrice(new BigDecimal("100"));

        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        order.setAccount(account);

        return order;
    }
}
