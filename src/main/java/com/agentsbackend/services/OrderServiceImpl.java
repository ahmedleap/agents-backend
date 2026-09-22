package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.exceptions.OrderNotFoundException;
import com.agentsbackend.exceptions.InvalidOrderStatusException;
import com.agentsbackend.exceptions.InvalidOrderParametersException;
import com.agentsbackend.exceptions.InsufficientFundsException;
import com.agentsbackend.exceptions.InvalidAccountException;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.HoldingsRepository;
import com.agentsbackend.repos.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final HoldingsRepository holdingsRepository;
    private final AccountRepository accountRepository;

    public OrderServiceImpl(OrderRepository orderRepository, HoldingsRepository holdingsRepository, 
                          AccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.holdingsRepository = holdingsRepository;
        this.accountRepository = accountRepository;
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
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // Run all validation checks in order: basic validations first, then DB checks
        validateOrderQuantity(request);
        validatePositionLimit(request);
        validateCashBalance(request);
        
        // Implementation continues...
        return null;
    }

    /**
     * CHECK 3: Validates order quantity is valid
     * - Must be > 0
     * - Must be an integer (no fractional shares)
     * - Must be <= 1,000,000
     */
    private void validateOrderQuantity(CreateOrderRequest request) {
        int quantity = request.getQuantity();
        
        if (quantity <= 0) {
            throw new InvalidOrderParametersException(
                "Order quantity must be greater than 0. Provided: " + quantity
            );
        }
        
        if (quantity > 1_000_000) {
            throw new InvalidOrderParametersException(
                "Order quantity exceeds maximum. Provided: " + quantity + 
                ", Maximum: 1,000,000"
            );
        }
    }

    /**
     * CHECK 1: Validates that current holding + new quantity does not exceed 10,000 shares
     */
    private void validatePositionLimit(CreateOrderRequest request) {
        Holding currentHolding = holdingsRepository.findByAccountAndInstrument(
            request.getAccountId(), 
            request.getInstrumentId()
        );
        
        int totalQuantity = request.getQuantity();
        if (currentHolding != null) {
            totalQuantity += currentHolding.getQuantity().intValue();
        }
        
        if (totalQuantity > 10000) {
            throw new InvalidOrderParametersException(
                "Order exceeds position limit. Current holding: " + 
                (currentHolding != null ? currentHolding.getQuantity() : 0) + 
                ", New order: " + request.getQuantity() + 
                ", Total would be: " + totalQuantity + 
                " (max: 10,000)"
            );
        }
    }

    /**
     * CHECK 2: Validates that account has sufficient cash balance for the order
     */
    private void validateCashBalance(CreateOrderRequest request) {
        Account account = accountRepository.findById(request.getAccountId());
        
        if (account == null) {
            throw new InvalidAccountException("Account not found");
        }
        
        BigDecimal requiredFunds = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
        
        if (account.getCashBalance().compareTo(requiredFunds) < 0) {
            throw new InsufficientFundsException(
                "Insufficient cash balance. Required: $" + requiredFunds + 
                ", Available: $" + account.getCashBalance()
            );
        }
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
