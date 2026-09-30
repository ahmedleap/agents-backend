package com.agentsbackend.controllers;

import com.agentsbackend.services.OrderService;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.enums.OrderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("OrderController Tests")
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderController orderController = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create order with valid request")
    void testCreateOrderSuccess() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(accountId);
        request.setInstrumentId(instrumentId);
        request.setQuantity(100);
        request.setOrderType(OrderType.BUY);

        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(UUID.randomUUID());
        response.setAccountId(accountId);
        response.setInstrumentId(instrumentId);
        response.setQuantity(100);
        response.setOrderType(OrderType.BUY);
        response.setMessage("Order created successfully");

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountId").value(accountId.toString()))
            .andExpect(jsonPath("$.message").value("Order created successfully"));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should cancel order with valid request")
    void testCancelOrderSuccess() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();

        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);

        CancelOrderResponse response = new CancelOrderResponse();
        response.setOrderId(orderId);
        response.setMessage("Order cancelled successfully");

        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        verify(orderService).cancelOrder(any(CancelOrderRequest.class));
    }

    @Test
    @DisplayName("Should retrieve pending orders successfully")
    void testGetPendingOrders() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/pending")
                .param("accountId", accountId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(orderService).getPendingOrders(any());
    }

    @Test
    @DisplayName("Should retrieve pending orders without account filter")
    void testGetPendingOrdersWithoutFilter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/pending")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(orderService).getPendingOrders(any());
    }

    @Test
    @DisplayName("Should retrieve order by ID successfully")
    void testGetOrderById() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(orderService).getOrderById(orderId);
    }

    @Test
    @DisplayName("Should retrieve order history for account")
    void testGetOrderHistory() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                .param("accountId", accountId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(orderService).getOrderHistory(accountId);
    }

    @Test
    @DisplayName("Should return HTTP 201 for successful order creation")
    void testCreateOrderHttpStatus() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAccountId(UUID.randomUUID());
        request.setInstrumentId(UUID.randomUUID());
        request.setQuantity(50);
        request.setOrderType(OrderType.SELL);

        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(UUID.randomUUID());
        response.setQuantity(50);
        response.setOrderType(OrderType.SELL);

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle multiple order cancellations")
    void testMultipleCancelOrders() throws Exception {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        CancelOrderRequest request1 = new CancelOrderRequest();
        request1.setOrderId(orderId1);

        CancelOrderResponse response1 = new CancelOrderResponse();
        response1.setOrderId(orderId1);

        CancelOrderRequest request2 = new CancelOrderRequest();
        request2.setOrderId(orderId2);

        CancelOrderResponse response2 = new CancelOrderResponse();
        response2.setOrderId(orderId2);

        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
            .thenReturn(response1, response2);

        // Act & Assert - First cancellation
        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk());

        // Act & Assert - Second cancellation
        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isOk());

        verify(orderService, times(2)).cancelOrder(any(CancelOrderRequest.class));
    }
}
