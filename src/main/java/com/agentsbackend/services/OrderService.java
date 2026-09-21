package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import java.util.List;

public interface OrderService {

    // Returns a list of all currently pending orders
    List<Order> getPendingOrders();
}
