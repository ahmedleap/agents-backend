package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

/**
 * Top traded instruments across the entire platform.
 * Ranked by volume, value, or popularity.
 */
public class TopInstrumentResponse {
    private String ticker;
    private String instrumentName;
    private String assetClass;
    private String industry;
    private Long tradeCount;
    private BigDecimal totalVolume;
    private BigDecimal totalVolumeValue;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal bidAskSpread;
    private Long uniqueTraders;
    private BigDecimal dayChangePercent;

    public TopInstrumentResponse() {}

    public TopInstrumentResponse(String ticker, String instrumentName, String assetClass,
                                Long tradeCount, BigDecimal totalVolume) {
        this.ticker = ticker;
        this.instrumentName = instrumentName;
        this.assetClass = assetClass;
        this.tradeCount = tradeCount;
        this.totalVolume = totalVolume;
    }

    // Getters and Setters
    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
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

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getBidAskSpread() {
        return bidAskSpread;
    }

    public void setBidAskSpread(BigDecimal bidAskSpread) {
        this.bidAskSpread = bidAskSpread;
    }

    public Long getUniqueTraders() {
        return uniqueTraders;
    }

    public void setUniqueTraders(Long uniqueTraders) {
        this.uniqueTraders = uniqueTraders;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }
}
