package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

/**
 * Order activity and fulfillment metrics across the entire platform.
 */
public class OrderMetricsResponse {
    private Long totalOrders;
    private Long filledOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private BigDecimal fulfillmentRate;
    private BigDecimal cancellationRate;
    private BigDecimal averageOrderQuantity;
    private BigDecimal averageOrderValue;
    private BigDecimal totalQuantityTraded;
    private BigDecimal totalValueTraded;
    private Long buyOrderCount;
    private Long sellOrderCount;
    private BigDecimal buyOrderPercent;
    private BigDecimal sellOrderPercent;
    private BigDecimal averageOrderDurationSeconds;
    private BigDecimal averageSpreadSlippage;

    public OrderMetricsResponse() {}

    // Getters and Setters
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

    public BigDecimal getFulfillmentRate() {
        return fulfillmentRate;
    }

    public void setFulfillmentRate(BigDecimal fulfillmentRate) {
        this.fulfillmentRate = fulfillmentRate;
    }

    public BigDecimal getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(BigDecimal cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public BigDecimal getAverageOrderQuantity() {
        return averageOrderQuantity;
    }

    public void setAverageOrderQuantity(BigDecimal averageOrderQuantity) {
        this.averageOrderQuantity = averageOrderQuantity;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public BigDecimal getTotalQuantityTraded() {
        return totalQuantityTraded;
    }

    public void setTotalQuantityTraded(BigDecimal totalQuantityTraded) {
        this.totalQuantityTraded = totalQuantityTraded;
    }

    public BigDecimal getTotalValueTraded() {
        return totalValueTraded;
    }

    public void setTotalValueTraded(BigDecimal totalValueTraded) {
        this.totalValueTraded = totalValueTraded;
    }

    public Long getBuyOrderCount() {
        return buyOrderCount;
    }

    public void setBuyOrderCount(Long buyOrderCount) {
        this.buyOrderCount = buyOrderCount;
    }

    public Long getSellOrderCount() {
        return sellOrderCount;
    }

    public void setSellOrderCount(Long sellOrderCount) {
        this.sellOrderCount = sellOrderCount;
    }

    public BigDecimal getBuyOrderPercent() {
        return buyOrderPercent;
    }

    public void setBuyOrderPercent(BigDecimal buyOrderPercent) {
        this.buyOrderPercent = buyOrderPercent;
    }

    public BigDecimal getSellOrderPercent() {
        return sellOrderPercent;
    }

    public void setSellOrderPercent(BigDecimal sellOrderPercent) {
        this.sellOrderPercent = sellOrderPercent;
    }

    public BigDecimal getAverageOrderDurationSeconds() {
        return averageOrderDurationSeconds;
    }

    public void setAverageOrderDurationSeconds(BigDecimal averageOrderDurationSeconds) {
        this.averageOrderDurationSeconds = averageOrderDurationSeconds;
    }

    public BigDecimal getAverageSpreadSlippage() {
        return averageSpreadSlippage;
    }

    public void setAverageSpreadSlippage(BigDecimal averageSpreadSlippage) {
        this.averageSpreadSlippage = averageSpreadSlippage;
    }
}
