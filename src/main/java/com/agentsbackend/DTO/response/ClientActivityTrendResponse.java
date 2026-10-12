package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Client activity trend analytics response DTO.
 * Tracks trading activity trends for individual clients over time.
 */
public class ClientActivityTrendResponse {
    private LocalDate date;
    private UUID clientId;
    private String clientName;
    private Long tradeCount;
    private Long totalVolume;
    private BigDecimal totalValue;
    private BigDecimal averageOrderValue;
    private BigDecimal orderSuccessRate;

    public ClientActivityTrendResponse() {}

    public ClientActivityTrendResponse(LocalDate date, UUID clientId, String clientName, 
                                       Long tradeCount, Long totalVolume, BigDecimal totalValue,
                                       BigDecimal averageOrderValue, BigDecimal orderSuccessRate) {
        this.date = date;
        this.clientId = clientId;
        this.clientName = clientName;
        this.tradeCount = tradeCount;
        this.totalVolume = totalVolume;
        this.totalValue = totalValue;
        this.averageOrderValue = averageOrderValue;
        this.orderSuccessRate = orderSuccessRate;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public BigDecimal getOrderSuccessRate() {
        return orderSuccessRate;
    }

    public void setOrderSuccessRate(BigDecimal orderSuccessRate) {
        this.orderSuccessRate = orderSuccessRate;
    }
}
