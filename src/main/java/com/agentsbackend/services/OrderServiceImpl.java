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
import com.agentsbackend.queue.OrderQueue;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final HoldingsRepository holdingsRepository;
    private final AccountRepository accountRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;
    private final OrderQueue orderQueue;
    private final AuditTrailService auditTrailService;

    public OrderServiceImpl(OrderRepository orderRepository, HoldingsRepository holdingsRepository, 
                          AccountRepository accountRepository, InstrumentPriceRepository instrumentPriceRepository,
                          OrderQueue orderQueue, AuditTrailService auditTrailService) {
        this.orderRepository = orderRepository;
        this.holdingsRepository = holdingsRepository;
        this.accountRepository = accountRepository;
        this.instrumentPriceRepository = instrumentPriceRepository;
        this.orderQueue = orderQueue;
        this.auditTrailService = auditTrailService;
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
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // validateOrderQuantity() is now handled by @Valid @Min @Max on DTO
            // validateOrderPrice() basic validation (>0) is now handled by @Valid @DecimalMin on DTO
            // Still need to validate market price exists and ±50% range for limit orders
            validateOrderPrice(request);
            validateClientAge(request);
            validateDuplicateOrder(request);
            
            // Order type-specific validations
            if (request.getOrderType().equals(OrderType.BUY)) {
                validatePositionLimit(request);
                validateCashBalance(request);
            } else if (request.getOrderType().equals(OrderType.SELL)) {
                validateSellingHoldings(request);
            }
            
            validateMinimumBalance(request);

            // All validations passed - create and persist the order
            Order order = new Order();
            order.setOrderId(orderId);
            
            Account account = new Account();
            account.setAccountId(request.getAccountId());
            order.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(request.getInstrumentId());
            order.setInstrument(instrument);
            
            order.setQuantity(new BigDecimal(request.getQuantity()));
            // Only set limitPrice for LIMIT orders (when price was provided by client)
            // For MARKET orders, limitPrice stays null
            order.setLimitPrice(request.getPrice());
            order.setOrderType(request.getOrderType());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(now);
            
            // Save order to database
            orderRepository.save(order);
            
            // Add order to queue for fulfillment processing
            orderQueue.enqueue(order);
            
            // Log order creation to audit trail
            Account fullAccount = accountRepository.findById(request.getAccountId());
            if (fullAccount != null && fullAccount.getClientId() != null) {
                auditTrailService.logOrderCreated(
                    orderId,
                    request.getAccountId(),
                    fullAccount.getClientId(),
                    request.getOrderType(),
                    request.getQuantity(),
                    request.getPrice()
                );
            } else {
                logger.warn("Could not log order creation for order {}: Account or clientId not found", orderId);
            }
            
            // For response, calculate totalValue
            BigDecimal priceForResponse = request.getPrice();
            BigDecimal totalValue = BigDecimal.ZERO;
            
            if (priceForResponse != null) {
                // Limit order - use the provided price
                totalValue = priceForResponse.multiply(new BigDecimal(request.getQuantity()));
            } else {
                // Market order - fetch current market price for estimated value
                InstrumentPrice currentPrice = instrumentPriceRepository.findLatestPrice(request.getInstrumentId());
                if (currentPrice != null && currentPrice.getPrice() != null) {
                    totalValue = currentPrice.getPrice().multiply(new BigDecimal(request.getQuantity()));
                }
            }
            
            CreateOrderResponse response = new CreateOrderResponse(
                orderId,
                request.getAccountId(),
                request.getInstrumentId(),
                request.getQuantity(),
                priceForResponse,  // null for market orders
                totalValue,
                request.getOrderType(),
                OrderStatus.PENDING,
                now,
                "Order created successfully"
            );
            
            return response;
        } catch (InvalidOrderParametersException | InsufficientFundsException | InvalidAccountException e) {
            // Expected validation exceptions - persist rejected order and re-throw with orderId
            try {
                // Since @Valid on controller ensures quantity and price are valid,
                // rejected orders due to business logic (cash balance, position limit, etc)
                // can safely be persisted
                Order rejectedOrder = new Order();
                rejectedOrder.setOrderId(orderId);
                
                Account account = new Account();
                account.setAccountId(request.getAccountId());
                rejectedOrder.setAccount(account);
                
                Instrument instrument = new Instrument();
                instrument.setInstrumentId(request.getInstrumentId());
                rejectedOrder.setInstrument(instrument);
                
                rejectedOrder.setQuantity(new BigDecimal(request.getQuantity()));
                rejectedOrder.setLimitPrice(request.getPrice());
                rejectedOrder.setOrderType(request.getOrderType());
                rejectedOrder.setStatus(OrderStatus.REJECTED);
                rejectedOrder.setCreatedAt(now);
                
                // Save rejected order to database
                orderRepository.save(rejectedOrder);
                
                // Log rejection to audit trail
                Account fullAccount = accountRepository.findById(request.getAccountId());
                if (fullAccount != null && fullAccount.getClientId() != null) {
                    auditTrailService.logOrderRejected(
                        orderId,
                        request.getAccountId(),
                        fullAccount.getClientId(),
                        e.getMessage(),
                        request.getOrderType(),
                        request.getQuantity(),
                        request.getPrice()
                    );
                } else {
                    logger.warn("Could not log rejection for order {}: Account or clientId not found", orderId);
                }
            } catch (Exception logException) {
                logger.error("Error persisting rejected order {}: {}", orderId, logException.getMessage(), logException);
            }
            
            // Attach orderId to the validation exception before re-throwing
            if (e instanceof InvalidOrderParametersException) {
                ((InvalidOrderParametersException) e).setOrderId(orderId);
            } else if (e instanceof InsufficientFundsException) {
                ((InsufficientFundsException) e).setOrderId(orderId);
            } else if (e instanceof InvalidAccountException) {
                ((InvalidAccountException) e).setOrderId(orderId);
            }
            throw e;
        } catch (Exception unexpectedException) {
            // Unexpected exceptions - log and wrap as validation error
            logger.error("Unexpected error creating order: {}", unexpectedException.getMessage(), unexpectedException);
            throw new InvalidOrderParametersException(
                "Error processing order: " + unexpectedException.getMessage()
            );
        }
    }

    // Validates/fetches order price
    //  For LIMIT orders (price provided): validates price is reasonable (within ±50% of market)
    //    Basic price > 0 validation is handled by @DecimalMin on DTO
    //  For MARKET orders (price omitted): validates that market price exists
     
    private void validateOrderPrice(CreateOrderRequest request) {
        BigDecimal price = request.getPrice();
        
        InstrumentPrice latestPrice = instrumentPriceRepository.findLatestPrice(request.getInstrumentId());
        
        if (price == null) {
            // MARKET ORDER - validate market price exists
            if (latestPrice == null) {
                throw new InvalidOrderParametersException(
                    "Market order cannot be executed. No market price available for instrument: " + 
                    request.getInstrumentId()
                );
            }
        } else {
            // LIMIT ORDER - price > 0 already validated by @DecimalMin on DTO
            // Now validate it's within ±50% of market price
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

    // Validates that the client is at least 18 years old
    private void validateClientAge(CreateOrderRequest request) {
        // Fetch client's date of birth directly via query to avoid lazy-loading issues
        java.time.LocalDate dateOfBirth = accountRepository.findClientDateOfBirthByAccountId(request.getAccountId());
        
        if (dateOfBirth == null) {
            throw new InvalidAccountException("Account not found or client date of birth not available");
        }
        
        // Calculate age
        java.time.LocalDate today = java.time.LocalDate.now();
        int age = today.getYear() - dateOfBirth.getYear();
        
        // Adjust if birthday hasn't occurred this year yet
        if (today.getMonthValue() < dateOfBirth.getMonthValue() ||
            (today.getMonthValue() == dateOfBirth.getMonthValue() && 
             today.getDayOfMonth() < dateOfBirth.getDayOfMonth())) {
            age--;
        }
        
        if (age < 18) {
            throw new InvalidOrderParametersException(
                "Client must be at least 18 years old to place trades. Current age: " + age
            );
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

    // Validates that account has sufficient shares to sell
    private void validateSellingHoldings(CreateOrderRequest request) {
        Holding currentHolding = holdingsRepository.findByAccountAndInstrument(
            request.getAccountId(), 
            request.getInstrumentId()
        );
        
        if (currentHolding == null) {
            throw new InvalidOrderParametersException(
                "You do not own any shares of this instrument. Cannot place a SELL order."
            );
        }
        
        int quantityToSell = request.getQuantity();
        int quantityOwned = currentHolding.getQuantity().intValue();
        
        if (quantityOwned < quantityToSell) {
            throw new InvalidOrderParametersException(
                "Insufficient shares to sell. You own: " + quantityOwned + 
                " shares, trying to sell: " + quantityToSell + " shares"
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
        
        // For market orders (price == null), we need to fetch market price for validation
        BigDecimal priceForValidation = request.getPrice();
        if (priceForValidation == null) {
            InstrumentPrice latestPrice = instrumentPriceRepository.findLatestPrice(request.getInstrumentId());
            if (latestPrice != null) {
                priceForValidation = latestPrice.getPrice();
            }
        }
        
        if (priceForValidation == null) {
            throw new InvalidOrderParametersException(
                "Order price not available. Market price may not be available."
            );
        }
        
        BigDecimal requiredFunds = priceForValidation.multiply(new BigDecimal(request.getQuantity()));
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
            InstrumentPrice latestPrice = instrumentPriceRepository.findLatestPrice(holding.getInstrumentId());
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
        
        // Log order cancellation to audit trail
        Account fullAccount = accountRepository.findById(order.getAccount().getAccountId());
        if (fullAccount != null && fullAccount.getClientId() != null) {
            auditTrailService.logOrderCancelled(
                order.getOrderId(),
                order.getAccount().getAccountId(),
                fullAccount.getClientId(),
                order.getOrderType(),
                order.getQuantity().intValue(),
                order.getLimitPrice(),
                "Order cancelled by user"
            );
        } else {
            logger.warn("Could not log order cancellation for order {}: Account or clientId not found", order.getOrderId());
        }
        
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
