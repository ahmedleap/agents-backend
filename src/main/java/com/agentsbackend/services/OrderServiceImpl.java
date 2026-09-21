package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.exceptions.OrderNotFoundException;
import com.agentsbackend.exceptions.InvalidOrderStatusException;

import java.time.LocalDateTime;
import java.util.List;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.repos.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Returns a list of pending orders based on filter criteria (accountId, limit, offset)
    @Override
    public List<Order> getPendingOrders(GetPendingOrdersRequest request) {
        // If accountId is provided, filter by that account
        if (request.getAccountId() != null) {
            return orderRepository.findPendingOrdersByAccount(request.getAccountId());
        }
        // Otherwise return all pending orders
        return orderRepository.findPendingOrders();
    }

@Override
public CancelOrderResponse cancelOrder(CancelOrderRequest request) {

    Order order = orderRepository.findById(request.getOrderId());
    
    if (order == null) {
        throw new OrderNotFoundException("Order not found");
    }
    
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
        throw new InvalidOrderStatusException(
            "Order cannot be cancelled. Current status: " + order.getStatus()
        );
    }
    

    order.setStatus(OrderStatus.CANCELLED);
    
    // order.setCancellationReason(request.getCancellationReason());

    order.setCancelledAt(LocalDateTime.now());
    orderRepository.updateOrder(order);
    
    return new CancelOrderResponse(
        order.getOrderId(),
        order.getStatus(),
        order.getCancelledAt(),
        "Order cancelled successfully"
    );
}
}
