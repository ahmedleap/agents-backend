package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

/**
 * Asset class distribution metrics across the entire platform.
 */
public class AssetClassMetricsResponse {
    private String assetClass;
    private Long instrumentCount;
    private Long tradeCount;
    private BigDecimal totalVolume;
    private BigDecimal totalVolumeValue;
    private BigDecimal percentOfTotalVolume;
    private BigDecimal percentOfTotalValue;
    private BigDecimal averagePrice;
    private BigDecimal priceVolatility;
    private Long uniqueTraders;

    public AssetClassMetricsResponse() {}

    public AssetClassMetricsResponse(String assetClass, Long tradeCount, BigDecimal totalVolume) {
        this.assetClass = assetClass;
        this.tradeCount = tradeCount;
        this.totalVolume = totalVolume;
    }

    // Getters and Setters
    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public Long getInstrumentCount() {
        return instrumentCount;
    }

    public void setInstrumentCount(Long instrumentCount) {
        this.instrumentCount = instrumentCount;
    }

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public BigDecimal getTotalVolumeValue() {
        return totalVolumeValue;
    }

    public void setTotalVolumeValue(BigDecimal totalVolumeValue) {
        this.totalVolumeValue = totalVolumeValue;
    }

    public BigDecimal getPercentOfTotalVolume() {
        return percentOfTotalVolume;
    }

    public void setPercentOfTotalVolume(BigDecimal percentOfTotalVolume) {
        this.percentOfTotalVolume = percentOfTotalVolume;
    }

    public BigDecimal getPercentOfTotalValue() {
        return percentOfTotalValue;
    }

    public void setPercentOfTotalValue(BigDecimal percentOfTotalValue) {
        this.percentOfTotalValue = percentOfTotalValue;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getPriceVolatility() {
        return priceVolatility;
    }

    public void setPriceVolatility(BigDecimal priceVolatility) {
        this.priceVolatility = priceVolatility;
    }

    public Long getUniqueTraders() {
        return uniqueTraders;
    }

    public void setUniqueTraders(Long uniqueTraders) {
        this.uniqueTraders = uniqueTraders;
    }
}
