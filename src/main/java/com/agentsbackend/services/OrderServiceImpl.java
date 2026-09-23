package com.agentsbackend.services;
import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.InstrumentPrice;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.exceptions.OrderNotFoundException;
import com.agentsbackend.exceptions.InvalidOrderStatusException;
import com.agentsbackend.exceptions.InvalidOrderParametersException;
import com.agentsbackend.exceptions.InsufficientFundsException;
import com.agentsbackend.exceptions.InvalidAccountException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.math.BigDecimal;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.DTO.response.OrderSummaryResponse;
import com.agentsbackend.repos.OrderRepository;
import com.agentsbackend.repos.HoldingsRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final HoldingsRepository holdingsRepository;
    private final AccountRepository accountRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;

    public OrderServiceImpl(OrderRepository orderRepository, HoldingsRepository holdingsRepository, 
                          AccountRepository accountRepository, InstrumentPriceRepository instrumentPriceRepository) {
        this.orderRepository = orderRepository;
        this.holdingsRepository = holdingsRepository;
        this.accountRepository = accountRepository;
        this.instrumentPriceRepository = instrumentPriceRepository;
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
        validateOrderQuantity(request);
        validateOrderPrice(request);
        validateDuplicateOrder(request);
        validatePositionLimit(request);
        validateCashBalance(request);
        validateMinimumBalance(request);

        // All validations passed - create and persist the order
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        Order order = new Order();
        order.setOrderId(orderId);
        
        Account account = new Account();
        account.setAccountId(request.getAccountId());
        order.setAccount(account);
        
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(request.getInstrumentId());
        order.setInstrument(instrument);
        
        order.setQuantity(new BigDecimal(request.getQuantity()));
        order.setLimitPrice(request.getPrice());
        order.setOrderType(request.getOrderType());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(now);
        
        orderRepository.save(order);
        
        BigDecimal totalValue = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
        CreateOrderResponse response = new CreateOrderResponse(
            orderId,
            request.getAccountId(),
            request.getInstrumentId(),
            request.getQuantity(),
            request.getPrice(),
            totalValue,
            request.getOrderType(),
            OrderStatus.PENDING,
            now,
            "Order created successfully"
        );
        
        return response;
    }

    // Validates order quantity is within acceptable limits
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

    // Validates/fetches order price
    //  For LIMIT orders (price provided): validates price is reasonable (> 0, within ±50% of market)
    //  For MARKET orders (price omitted): fetches latest market price and updates request
     
    private void validateOrderPrice(CreateOrderRequest request) {
        BigDecimal price = request.getPrice();
        
        InstrumentPrice latestPrice = instrumentPriceRepository.findLatestPrice(request.getInstrumentId());
        
        if (price == null) {
            // MARKET ORDER
            if (latestPrice == null) {
                throw new InvalidOrderParametersException(
                    "Market order cannot be executed. No market price available for instrument: " + 
                    request.getInstrumentId()
                );
            }
            request.setPrice(latestPrice.getPrice());
        } else {
            // LIMIT ORDER
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderParametersException(
                    "Order price must be greater than 0. Provided: $" + price
                );
            }
            
            
            if (latestPrice != null) {
                BigDecimal marketPrice = latestPrice.getPrice();
                BigDecimal upperBound = marketPrice.multiply(new BigDecimal("1.50"));
                BigDecimal lowerBound = marketPrice.multiply(new BigDecimal("0.50"));
                
                if (price.compareTo(upperBound) > 0) {
                    throw new InvalidOrderParametersException(
                        "Order price exceeds 50% above market price. Ordered: $" + price + 
                        ", Market: $" + marketPrice + ", Max allowed: $" + upperBound
                    );
                }
                
                if (price.compareTo(lowerBound) < 0) {
                    throw new InvalidOrderParametersException(
                        "Order price is 50% below market price. Ordered: $" + price + 
                        ", Market: $" + marketPrice + ", Min allowed: $" + lowerBound
                    );
                }
            }
        }
    }

    // Validates that an identical order was not placed in the last 60 seconds
    private void validateDuplicateOrder(CreateOrderRequest request) {
        int duplicateCount = orderRepository.countDuplicateOrders(
            request.getAccountId(),
            request.getInstrumentId(),
            new BigDecimal(request.getQuantity()),
            request.getPrice()
        );
        
        if (duplicateCount > 0) {
            throw new InvalidOrderParametersException(
                "Duplicate order detected. An identical order was placed within the last 60 seconds. " +
                "Please wait before placing another order."
            );
        }
    }

    // Validates that current holding + new quantity does not exceed 10,000 shares
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

    // Validates that account has sufficient cash balance for the order
    // Also checks that post-fill cash balance meets minimum balance requirements
    private void validateCashBalance(CreateOrderRequest request) {
        Account account = accountRepository.findById(request.getAccountId());
        
        if (account == null) {
            throw new InvalidAccountException("Account not found");
        }
        
        BigDecimal requiredFunds = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
        BigDecimal currentCash = account.getCashBalance();
        
        if (currentCash.compareTo(requiredFunds) < 0) {
            throw new InsufficientFundsException(
                "Insufficient cash balance. Required: $" + requiredFunds + 
                ", Available: $" + currentCash
            );
        }
        
        BigDecimal cashAfterFill = currentCash.subtract(requiredFunds);
        
        if (cashAfterFill.compareTo(new BigDecimal("0.1")) < 0) {
            throw new InsufficientFundsException(
                "Order would leave account balance below minimum $0.10. " +
                "Current: $" + currentCash + ", After order: $" + cashAfterFill + 
                " (need at least $0.10 remaining)"
            );
        }
        
        if (request.getOrderType().equals(OrderType.BUY)) {
            BigDecimal totalEquity = calculateTotalEquity(request.getAccountId());
            
            if (totalEquity.compareTo(new BigDecimal("5")) < 0) {
                if (cashAfterFill.compareTo(new BigDecimal("5")) < 0) {
                    throw new InsufficientFundsException(
                        "BUY order would violate minimum balance. Current equity: $" + totalEquity + 
                        ", Cash after order: $" + cashAfterFill + " (need at least $5 remaining when equity < $5)"
                    );
                }
            }
        }
    }

    /**
     * Validates minimum balance restrictions
     * - If cash balance < $0.1: reject ALL orders (BUY and SELL)
     * - If cash balance < $5 AND total equity < $5: reject BUY orders only (allow SELL for recovery)
     */
    private void validateMinimumBalance(CreateOrderRequest request) {
        Account account = accountRepository.findById(request.getAccountId());
        
        if (account == null) {
            throw new InvalidAccountException("Account not found");
        }
        
        BigDecimal cashBalance = account.getCashBalance();
        
        if (cashBalance.compareTo(new BigDecimal("0.1")) < 0) {
            throw new InsufficientFundsException(
                "Account balance too low. Minimum $0.10 required for trading. Current balance: $" + cashBalance
            );
        }
        
        if (cashBalance.compareTo(new BigDecimal("5")) < 0) {
            BigDecimal totalEquity = calculateTotalEquity(request.getAccountId());
            
            if (totalEquity.compareTo(new BigDecimal("5")) < 0) {
                // Only reject BUY orders; allow SELL for recovery
                if (request.getOrderType().equals(OrderType.BUY)) {
                    throw new InsufficientFundsException(
                        "Account equity below minimum of $5. Total equity: $" + totalEquity + 
                        ". BUY orders restricted. You may SELL to recover funds."
                    );
                }
            }
        }
    }

   
    private BigDecimal calculateTotalEquity(UUID accountId) {
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cashBalance = account.getCashBalance();
        BigDecimal holdingsValue = BigDecimal.ZERO;
        
        // Fetch all holdings for this account
        List<Holding> holdings = holdingsRepository.findAllByAccount(accountId);
        
        // Calculate value of each holding using current market price
        for (Holding holding : holdings) {
            InstrumentPrice latestPrice = instrumentPriceRepository.findLatestPrice(holding.getInstrument().getInstrumentId());
            if (latestPrice != null) {
                BigDecimal holdingValue = holding.getQuantity().multiply(latestPrice.getPrice());
                holdingsValue = holdingsValue.add(holdingValue);
            }
        }
        
        return cashBalance.add(holdingsValue);
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

    @Override
    public List<OrderSummaryResponse> getOrderHistory(UUID accountId) {
        Account account = accountRepository.findById(accountId);
        
        if (account == null) {
            throw new InvalidAccountException("Account not found");
        }
        
        List<Order> orders = orderRepository.findAllByAccount(accountId);
        List<OrderSummaryResponse> summaries = new ArrayList<>();
        
        for (Order order : orders) {
            summaries.add(new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderType(),
                order.getQuantity().intValue(),
                order.getLimitPrice(),
                order.getFilledPrice(),
                order.getStatus()
            ));
        }
        
        return summaries;
    }

    @Override
    public Order getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId);
        
        if (order == null) {
            throw new OrderNotFoundException("Order not found with ID: " + orderId);
        }
        
        return order;
    }
}
