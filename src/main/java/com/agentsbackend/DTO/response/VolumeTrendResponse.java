package com.agentsbackend.DTO.response;

import java.time.LocalDate;

/**
 * VolumeTrendResponse - DTO for trading volume trend data point.
 * Represents daily/weekly/monthly trading volume trends.
 */
public class VolumeTrendResponse {
    private LocalDate date;
    private Long totalQuantityTraded;
    private Double totalValueTraded;
    private Long tradeCount;
    private Double averageOrderValue;
    private Long buyCount;
    private Long sellCount;

    // Constructors
    public VolumeTrendResponse() {}

    public VolumeTrendResponse(LocalDate date, Long totalQuantityTraded, Double totalValueTraded,
                             Long tradeCount, Double averageOrderValue, Long buyCount, Long sellCount) {
        this.date = date;
        this.totalQuantityTraded = totalQuantityTraded;
        this.totalValueTraded = totalValueTraded;
        this.tradeCount = tradeCount;
        this.averageOrderValue = averageOrderValue;
        this.buyCount = buyCount;
        this.sellCount = sellCount;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTotalQuantityTraded() {
        return totalQuantityTraded;
    }

    public void setTotalQuantityTraded(Long totalQuantityTraded) {
        this.totalQuantityTraded = totalQuantityTraded;
    }

    public Double getTotalValueTraded() {
        return totalValueTraded;
    }

    public void setTotalValueTraded(Double totalValueTraded) {
        this.totalValueTraded = totalValueTraded;
    }

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Long getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(Long buyCount) {
        this.buyCount = buyCount;
    }

    public Long getSellCount() {
        return sellCount;
    }

    public void setSellCount(Long sellCount) {
        this.sellCount = sellCount;
    }

    @Override
    public String toString() {
        return "VolumeTrendResponse{" +
                "date=" + date +
                ", totalQuantityTraded=" + totalQuantityTraded +
                ", totalValueTraded=" + totalValueTraded +
                ", tradeCount=" + tradeCount +
                ", averageOrderValue=" + averageOrderValue +
                ", buyCount=" + buyCount +
                ", sellCount=" + sellCount +
                '}';
    }
}
