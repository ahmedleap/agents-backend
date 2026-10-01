package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.OrderStatus;

/**
 * Concise view of an order for order history listing.
 */
public class OrderSummaryResponse {

    private UUID orderId;
    private OrderType orderType;
    private Integer quantity;
    private BigDecimal limitPrice;
    private BigDecimal filledPrice;
    private OrderStatus status;

    public OrderSummaryResponse() {
    }

    public OrderSummaryResponse(UUID orderId, OrderType orderType, Integer quantity, 
                               BigDecimal limitPrice, BigDecimal filledPrice, OrderStatus status) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.filledPrice = filledPrice;
        this.status = status;
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public BigDecimal getFilledPrice() {
        return filledPrice;
    }

    public void setFilledPrice(BigDecimal filledPrice) {
        this.filledPrice = filledPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
