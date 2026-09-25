package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import java.math.BigDecimal;
import com.agentsbackend.enums.OrderType;

/**
 * Request DTO for creating a new order.
 * Price is optional: if provided, creates a LIMIT order; if omitted, creates a MARKET order
 */
public class CreateOrderRequest {

    @NotNull(message = "Account ID cannot be null")
    private UUID accountId;

    @NotNull(message = "Instrument ID cannot be null")
    private UUID instrumentId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Max(value = 1_000_000, message = "Quantity cannot exceed 1,000,000")
    private Integer quantity;

    @DecimalMin(value = "0.01", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;  // Optional: null = MARKET order, value = LIMIT order

    @NotNull(message = "Order type cannot be null")
    private OrderType orderType;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(UUID accountId, UUID instrumentId, Integer quantity, 
                             BigDecimal price, OrderType orderType) {
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}
