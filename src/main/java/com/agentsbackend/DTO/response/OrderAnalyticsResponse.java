package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;

/**
 * Order execution analytics response DTO.
 * Contains aggregated order metrics for a given account or time period.
 */
public class OrderAnalyticsResponse {
    private UUID accountId;
    private String orderType;
    private String status;
    private Long totalOrders;
    private Long filledOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private BigDecimal totalQuantity;
    private BigDecimal averageFilledPrice;
    private BigDecimal averageLimitPrice;
    private BigDecimal successRate;
    private BigDecimal averageDurationSeconds;
    private BigDecimal totalCommissionCost;

    public OrderAnalyticsResponse() {}

    public OrderAnalyticsResponse(UUID accountId, String orderType, String status,
                                 Long totalOrders, Long filledOrders) {
        this.accountId = accountId;
        this.orderType = orderType;
        this.status = status;
        this.totalOrders = totalOrders;
        this.filledOrders = filledOrders;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getFilledOrders() {
        return filledOrders;
    }

    public void setFilledOrders(Long filledOrders) {
        this.filledOrders = filledOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getAverageFilledPrice() {
        return averageFilledPrice;
    }

    public void setAverageFilledPrice(BigDecimal averageFilledPrice) {
        this.averageFilledPrice = averageFilledPrice;
    }

    public BigDecimal getAverageLimitPrice() {
        return averageLimitPrice;
    }

    public void setAverageLimitPrice(BigDecimal averageLimitPrice) {
        this.averageLimitPrice = averageLimitPrice;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public BigDecimal getAverageDurationSeconds() {
        return averageDurationSeconds;
    }

    public void setAverageDurationSeconds(BigDecimal averageDurationSeconds) {
        this.averageDurationSeconds = averageDurationSeconds;
    }

    public BigDecimal getTotalCommissionCost() {
        return totalCommissionCost;
    }

    public void setTotalCommissionCost(BigDecimal totalCommissionCost) {
        this.totalCommissionCost = totalCommissionCost;
    }
}
