package com.agentsbackend.entities;

import jakarta.persistence.*;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", columnDefinition = "UUID")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "order_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6, columnDefinition = "NUMERIC(18,6) CHECK (quantity > 0)")
    private BigDecimal quantity;

    @Column(name = "limit_price", precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (limit_price > 0)")
    private BigDecimal limitPrice;

    @Column(name = "filled_price", precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (filled_price > 0)")
    private BigDecimal filledPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public Order() {
    }

    public Order(UUID orderId, Account account, Instrument instrument, OrderType orderType,
                 BigDecimal quantity, BigDecimal limitPrice, BigDecimal filledPrice, OrderStatus status,
                 LocalDateTime createdAt, LocalDateTime filledAt, LocalDateTime cancelledAt) {
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
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFilledAt() {
        return filledAt;
    }

    public void setFilledAt(LocalDateTime filledAt) {
        this.filledAt = filledAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
