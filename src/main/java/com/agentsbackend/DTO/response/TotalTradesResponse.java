package com.agentsbackend.DTO.response;

/**
 * TotalTradesResponse - DTO for total trades metric.
 * Represents the total count of trades and their status breakdown on the platform.
 */
public class TotalTradesResponse {
    private Long totalTrades;
    private Long filledTrades;
    private Long cancelledTrades;
    private Long pendingTrades;
    private Double fulfillmentRate;
    private Double cancellationRate;
    private Long buyOrderCount;
    private Long sellOrderCount;
    private Double buyPercentage;
    private Double sellPercentage;

    // Constructors
    public TotalTradesResponse() {}

    public TotalTradesResponse(Long totalTrades, Long filledTrades, Long cancelledTrades, Long pendingTrades,
                             Double fulfillmentRate, Double cancellationRate, Long buyOrderCount, Long sellOrderCount,
                             Double buyPercentage, Double sellPercentage) {
        this.totalTrades = totalTrades;
        this.filledTrades = filledTrades;
        this.cancelledTrades = cancelledTrades;
        this.pendingTrades = pendingTrades;
        this.fulfillmentRate = fulfillmentRate;
        this.cancellationRate = cancellationRate;
        this.buyOrderCount = buyOrderCount;
        this.sellOrderCount = sellOrderCount;
        this.buyPercentage = buyPercentage;
        this.sellPercentage = sellPercentage;
    }

    // Getters and Setters
    public Long getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(Long totalTrades) {
        this.totalTrades = totalTrades;
    }

    public Long getFilledTrades() {
        return filledTrades;
    }

    public void setFilledTrades(Long filledTrades) {
        this.filledTrades = filledTrades;
    }

    public Long getCancelledTrades() {
        return cancelledTrades;
    }

    public void setCancelledTrades(Long cancelledTrades) {
        this.cancelledTrades = cancelledTrades;
    }

    public Long getPendingTrades() {
        return pendingTrades;
    }

    public void setPendingTrades(Long pendingTrades) {
        this.pendingTrades = pendingTrades;
    }

    public Double getFulfillmentRate() {
        return fulfillmentRate;
    }

    public void setFulfillmentRate(Double fulfillmentRate) {
        this.fulfillmentRate = fulfillmentRate;
    }

    public Double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(Double cancellationRate) {
        this.cancellationRate = cancellationRate;
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

    public Double getBuyPercentage() {
        return buyPercentage;
    }

    public void setBuyPercentage(Double buyPercentage) {
        this.buyPercentage = buyPercentage;
    }

    public Double getSellPercentage() {
        return sellPercentage;
    }

    public void setSellPercentage(Double sellPercentage) {
        this.sellPercentage = sellPercentage;
    }

    @Override
    public String toString() {
        return "TotalTradesResponse{" +
                "totalTrades=" + totalTrades +
                ", filledTrades=" + filledTrades +
                ", cancelledTrades=" + cancelledTrades +
                ", pendingTrades=" + pendingTrades +
                ", fulfillmentRate=" + fulfillmentRate +
                ", cancellationRate=" + cancellationRate +
                ", buyOrderCount=" + buyOrderCount +
                ", sellOrderCount=" + sellOrderCount +
                ", buyPercentage=" + buyPercentage +
                ", sellPercentage=" + sellPercentage +
                '}';
    }
}
