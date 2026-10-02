package com.agentsbackend.entities;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * FactOrder represents the Order fact from the warehouse schema.
 * Used for analytics queries against fact_orders table.
 */
public class FactOrder {
    private UUID orderId;
    private UUID accountId;
    private UUID instrumentId;
    private String orderType;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private BigDecimal filledPrice;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime filledAt;
    private ZonedDateTime cancelledAt;
    private String cancelReason;
    private BigDecimal durationSeconds;
    private ZonedDateTime extractTimestamp;

    // Constructors
    public FactOrder() {}

    public FactOrder(UUID orderId, UUID accountId, UUID instrumentId, String orderType,
                    BigDecimal quantity, BigDecimal limitPrice, String status, ZonedDateTime createdAt) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.orderType = orderType;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getFilledAt() {
        return filledAt;
    }

    public void setFilledAt(ZonedDateTime filledAt) {
        this.filledAt = filledAt;
    }

    public ZonedDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(ZonedDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public BigDecimal getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(BigDecimal durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ZonedDateTime getExtractTimestamp() {
        return extractTimestamp;
    }

    public void setExtractTimestamp(ZonedDateTime extractTimestamp) {
        this.extractTimestamp = extractTimestamp;
    }
}
