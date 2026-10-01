package com.agentsbackend.entities;

import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Order {

    private UUID orderId;
    private UUID accountId;
    private Account account;
    private Instrument instrument;
    private OrderType orderType;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private BigDecimal filledPrice;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime filledAt;
    private OffsetDateTime cancelledAt;
    private String cancelReason;

    public Order() {
    }

    public Order(UUID orderId, Account account, Instrument instrument, OrderType orderType,
                 BigDecimal quantity, BigDecimal limitPrice, BigDecimal filledPrice, OrderStatus status,
                 OffsetDateTime createdAt, OffsetDateTime filledAt, OffsetDateTime cancelledAt, String cancelReason) {
        this.orderId = orderId;
        this.account = account;
        this.instrument = instrument;
        this.orderType = orderType;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.filledPrice = filledPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.filledAt = filledAt;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
    }

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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getFilledAt() {
        return filledAt;
    }

    public void setFilledAt(OffsetDateTime filledAt) {
        this.filledAt = filledAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
