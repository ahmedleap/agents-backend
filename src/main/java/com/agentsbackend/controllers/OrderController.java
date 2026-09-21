package com.agentsbackend.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import com.agentsbackend.services.OrderService;
import com.agentsbackend.entities.Order;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // HTTP GET endpoint for viewing pending orders
    @GetMapping("/pending")
    public List<Order> getPendingOrders() {
        return orderService.getPendingOrders();
    }
    
}

// - post orders (order creation logic)
// - post order preview
// - get orders (history)
// - get specific order information
// - cancel orders
// - view pending orders