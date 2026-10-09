package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Asset class trend analytics response DTO.
 * Tracks trading activity by asset class over time.
 */
public class AssetClassTrendResponse {
    private LocalDate date;
    private String assetClass;
    private Long tradeCount;
    private Long totalVolume;
    private BigDecimal totalValue;
    private BigDecimal percentOfTotalVolume;
    private Long uniqueTraders;

    public AssetClassTrendResponse() {}

    public AssetClassTrendResponse(LocalDate date, String assetClass, Long tradeCount,
                                  Long totalVolume, BigDecimal totalValue,
                                  BigDecimal percentOfTotalVolume, Long uniqueTraders) {
        this.date = date;
        this.assetClass = assetClass;
        this.tradeCount = tradeCount;
        this.totalVolume = totalVolume;
        this.totalValue = totalValue;
        this.percentOfTotalVolume = percentOfTotalVolume;
        this.uniqueTraders = uniqueTraders;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
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

    public BigDecimal getPercentOfTotalVolume() {
        return percentOfTotalVolume;
    }

    public void setPercentOfTotalVolume(BigDecimal percentOfTotalVolume) {
        this.percentOfTotalVolume = percentOfTotalVolume;
    }

    public Long getUniqueTraders() {
        return uniqueTraders;
    }

    public void setUniqueTraders(Long uniqueTraders) {
        this.uniqueTraders = uniqueTraders;
    }
}
