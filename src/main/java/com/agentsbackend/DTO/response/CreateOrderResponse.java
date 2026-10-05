package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;

/**
 * Response DTO for created order confirmation.
 */
public class CreateOrderResponse {

    private UUID orderId;
    private UUID accountId;
    private UUID instrumentId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private OrderType orderType;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String message;

    public CreateOrderResponse() {
    }

    public CreateOrderResponse(UUID orderId, UUID accountId, UUID instrumentId, 
                              Integer quantity, BigDecimal price, BigDecimal totalValue,
                              OrderType orderType, OrderStatus status, LocalDateTime createdAt, String message) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = totalValue;
        this.orderType = orderType;
        this.status = status;
        this.createdAt = createdAt;
        this.message = message;
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

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

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
