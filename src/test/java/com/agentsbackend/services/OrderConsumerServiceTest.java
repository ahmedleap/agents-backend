package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderConsumerService Tests")
class OrderConsumerServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private HoldingService holdingsService;

    private OrderConsumerService orderConsumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderConsumerService = new OrderConsumerService(
            orderRepository,
            accountRepository,
            instrumentRepository,
            auditTrailService,
            holdingsService
        );
    }

    @Test
    @DisplayName("Should process order successfully when received from Kafka")
    void testProcessOrderReceived() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.BUY, new BigDecimal("50"));
        
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        instrument.setAsk(new BigDecimal("40"));  // Lower than limit, should fill
        instrument.setBid(new BigDecimal("35"));

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000"));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(holdingsService).updateHoldingsForBuy(any(Order.class), any(BigDecimal.class));
        doNothing().when(auditTrailService).logOrderFilled(any(), any(), any(), any(), anyInt(), any());

        // Act
        assertDoesNotThrow(() -> orderConsumerService.processOrder(order));

        // Assert
        verify(orderRepository).updateOrder(any(Order.class));
        verify(auditTrailService).logOrderFilled(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("Should fill BUY order when ASK price is less than limit price")
    void testFillBuyOrderWhenAskBelowLimit() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.BUY, new BigDecimal("50"));
        
        Instrument instrument = new Instrument();
        instrument.setAsk(new BigDecimal("45"));  // Below limit of 50
        instrument.setBid(new BigDecimal("40"));

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000"));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(holdingsService).updateHoldingsForBuy(any(), any());
        doNothing().when(auditTrailService).logOrderFilled(any(), any(), any(), any(), anyInt(), any());

        // Act
        orderConsumerService.processOrder(order);

        // Assert - order should be marked as FILLED
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertEquals(new BigDecimal("45"), order.getFilledPrice());
    }

    @Test
    @DisplayName("Should fill SELL order when BID price is greater than limit price")
    void testFillSellOrderWhenBidAboveLimit() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.SELL, new BigDecimal("30"));
        
        Instrument instrument = new Instrument();
        instrument.setAsk(new BigDecimal("40"));
        instrument.setBid(new BigDecimal("35"));  // Above limit of 30

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(holdingsService).updateHoldingsForSell(any());
        doNothing().when(auditTrailService).logOrderFilled(any(), any(), any(), any(), anyInt(), any());

        // Act
        orderConsumerService.processOrder(order);

        // Assert
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertEquals(new BigDecimal("35"), order.getFilledPrice());
    }

    @Test
    @DisplayName("Should not fill order when market price is unfavorable")
    void testOrderNotFilledWhenPriceUnfavorable() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.BUY, new BigDecimal("30"));
        order.setStatus(OrderStatus.PENDING);  // Should remain PENDING
        
        Instrument instrument = new Instrument();
        instrument.setAsk(new BigDecimal("50"));  // Above limit of 30
        instrument.setBid(new BigDecimal("45"));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));

        // Act
        orderConsumerService.processOrder(order);

        // Assert - order should remain PENDING
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderRepository, never()).updateOrder(any(Order.class));
    }

    @Test
    @DisplayName("Should fill market BUY order immediately at ASK price")
    void testMarketBuyOrderFilledAtAskPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.BUY, null);  // null = market order
        
        Instrument instrument = new Instrument();
        instrument.setAsk(new BigDecimal("50"));
        instrument.setBid(new BigDecimal("45"));

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClientId(clientId);
        account.setCashBalance(new BigDecimal("10000"));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(orderRepository).updateOrder(any(Order.class));
        doNothing().when(holdingsService).updateHoldingsForBuy(any(), any());
        doNothing().when(auditTrailService).logOrderFilled(any(), any(), any(), any(), anyInt(), any());

        // Act
        orderConsumerService.processOrder(order);

        // Assert
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertEquals(new BigDecimal("50"), order.getFilledPrice());
    }

    @Test
    @DisplayName("Should handle exception when instrument not found")
    void testProcessOrderWhenInstrumentNotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order order = createTestOrder(orderId, accountId, instrumentId, OrderType.BUY, new BigDecimal("50"));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception, just log warning
        assertDoesNotThrow(() -> orderConsumerService.processOrder(order));
        
        // Order should not be filled
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    // Helper method
    private Order createTestOrder(UUID orderId, UUID accountId, UUID instrumentId, 
                                 OrderType orderType, BigDecimal limitPrice) {
        Order order = new Order();
        order.setOrderId(orderId);
        
        Account account = new Account();
        account.setAccountId(accountId);
        order.setAccount(account);
        
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);
        order.setInstrument(instrument);
        
        order.setOrderType(orderType);
        order.setLimitPrice(limitPrice);
        order.setQuantity(new BigDecimal("100"));
        order.setStatus(OrderStatus.PENDING);
        
        return order;
    }
}
