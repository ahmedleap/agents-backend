package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

/**
 * Instrument analysis response DTO.
 * Contains detailed trading metrics for a specific instrument over a period.
 */
public class InstrumentAnalysisResponse {
    private String ticker;
    private String instrumentName;
    private String assetClass;
    private Long totalVolume;
    private BigDecimal totalValue;
    private Long tradeCount;
    private Long uniqueTraders;
    private BigDecimal averagePrice;
    private BigDecimal volumeTrend;
    private BigDecimal buyVsSellRatio;

    public InstrumentAnalysisResponse() {}

    public InstrumentAnalysisResponse(String ticker, String instrumentName, String assetClass,
                                     Long totalVolume, BigDecimal totalValue, Long tradeCount,
                                     Long uniqueTraders, BigDecimal averagePrice,
                                     BigDecimal volumeTrend, BigDecimal buyVsSellRatio) {
        this.ticker = ticker;
        this.instrumentName = instrumentName;
        this.assetClass = assetClass;
        this.totalVolume = totalVolume;
        this.totalValue = totalValue;
        this.tradeCount = tradeCount;
        this.uniqueTraders = uniqueTraders;
        this.averagePrice = averagePrice;
        this.volumeTrend = volumeTrend;
        this.buyVsSellRatio = buyVsSellRatio;
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

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Long getUniqueTraders() {
        return uniqueTraders;
    }

    public void setUniqueTraders(Long uniqueTraders) {
        this.uniqueTraders = uniqueTraders;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getVolumeTrend() {
        return volumeTrend;
    }

    public void setVolumeTrend(BigDecimal volumeTrend) {
        this.volumeTrend = volumeTrend;
    }

    public BigDecimal getBuyVsSellRatio() {
        return buyVsSellRatio;
    }

    public void setBuyVsSellRatio(BigDecimal buyVsSellRatio) {
        this.buyVsSellRatio = buyVsSellRatio;
    }
}
