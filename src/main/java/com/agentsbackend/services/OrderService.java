package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.DTO.response.OrderSummaryResponse;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Returns a list of pending orders based on filter criteria
    List<Order> getPendingOrders(GetPendingOrdersRequest request);

    // Creates a new order
    CreateOrderResponse createOrder(CreateOrderRequest request);

    // Cancels an existing order
    CancelOrderResponse cancelOrder(CancelOrderRequest request);

    // Returns concise order history for a specific account
    List<OrderSummaryResponse> getOrderHistory(UUID accountId);

    // Returns a specific order by ID
    Order getOrderById(UUID orderId);
}
