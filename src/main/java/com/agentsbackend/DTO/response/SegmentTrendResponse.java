package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Segment trend analytics response DTO.
 * Tracks trading activity by client segment (risk tolerance or portfolio size) over time.
 */
public class SegmentTrendResponse {
    private LocalDate date;
    private String segment;
    private Long tradeCount;
    private Long totalVolume;
    private BigDecimal totalValue;
    private Long activeClientCount;
    private BigDecimal averageOrderValue;

    public SegmentTrendResponse() {}

    public SegmentTrendResponse(LocalDate date, String segment, Long tradeCount,
                               Long totalVolume, BigDecimal totalValue,
                               Long activeClientCount, BigDecimal averageOrderValue) {
        this.date = date;
        this.segment = segment;
        this.tradeCount = tradeCount;
        this.totalVolume = totalVolume;
        this.totalValue = totalValue;
        this.activeClientCount = activeClientCount;
        this.averageOrderValue = averageOrderValue;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public Long getActiveClientCount() {
        return activeClientCount;
    }

    public void setActiveClientCount(Long activeClientCount) {
        this.activeClientCount = activeClientCount;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
}
