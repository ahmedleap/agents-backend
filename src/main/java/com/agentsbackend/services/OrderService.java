package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import java.util.List;

public interface OrderService {

    // Returns a list of pending orders based on filter criteria
    List<Order> getPendingOrders(GetPendingOrdersRequest request);

    CancelOrderResponse cancelOrder(CancelOrderRequest request);
}
