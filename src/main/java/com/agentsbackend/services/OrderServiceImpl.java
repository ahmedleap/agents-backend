package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import java.util.List;
import com.agentsbackend.repos.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Returns a list of all currently pending orders
    @Override
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }
    
}
