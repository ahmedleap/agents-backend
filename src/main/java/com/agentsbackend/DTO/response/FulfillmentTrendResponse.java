package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Order fulfillment trend analytics response DTO.
 * Tracks order fulfillment rates and metrics over time.
 */
public class FulfillmentTrendResponse {
    private LocalDate date;
    private Long totalOrders;
    private Long filledOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private BigDecimal fulfillmentRate;
    private BigDecimal cancellationRate;
    private Long averageOrderDurationSeconds;
    private LocalTime peakTradingTime;

    public FulfillmentTrendResponse() {}

    public FulfillmentTrendResponse(LocalDate date, Long totalOrders, Long filledOrders,
                                   Long cancelledOrders, Long pendingOrders,
                                   BigDecimal fulfillmentRate, BigDecimal cancellationRate,
                                   Long averageOrderDurationSeconds, LocalTime peakTradingTime) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.filledOrders = filledOrders;
        this.cancelledOrders = cancelledOrders;
        this.pendingOrders = pendingOrders;
        this.fulfillmentRate = fulfillmentRate;
        this.cancellationRate = cancellationRate;
        this.averageOrderDurationSeconds = averageOrderDurationSeconds;
        this.peakTradingTime = peakTradingTime;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Long getAverageOrderDurationSeconds() {
        return averageOrderDurationSeconds;
    }

    public void setAverageOrderDurationSeconds(Long averageOrderDurationSeconds) {
        this.averageOrderDurationSeconds = averageOrderDurationSeconds;
    }

    public LocalTime getPeakTradingTime() {
        return peakTradingTime;
    }

    public void setPeakTradingTime(LocalTime peakTradingTime) {
        this.peakTradingTime = peakTradingTime;
    }
}
