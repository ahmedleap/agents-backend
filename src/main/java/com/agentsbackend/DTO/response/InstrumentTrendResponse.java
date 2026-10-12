package com.agentsbackend.DTO.response;

import java.time.LocalDate;

/**
 * InstrumentTrendResponse - DTO for instrument trading activity trend data point.
 * Represents trading volume and activity trends for a specific instrument over time.
 */
public class InstrumentTrendResponse {
    private LocalDate date;
    private String ticker;
    private String instrumentName;
    private String assetClass;
    private Long tradeCount;
    private Long totalQuantity;
    private Double totalValue;
    private Double averagePrice;
    private Long uniqueTraders;

    // Constructors
    public InstrumentTrendResponse() {}

    public InstrumentTrendResponse(LocalDate date, String ticker, String instrumentName, String assetClass,
                                  Long tradeCount, Long totalQuantity, Double totalValue,
                                  Double averagePrice, Long uniqueTraders) {
        this.date = date;
        this.ticker = ticker;
        this.instrumentName = instrumentName;
        this.assetClass = assetClass;
        this.tradeCount = tradeCount;
        this.totalQuantity = totalQuantity;
        this.totalValue = totalValue;
        this.averagePrice = averagePrice;
        this.uniqueTraders = uniqueTraders;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

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

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public Long getUniqueTraders() {
        return uniqueTraders;
    }

    public void setUniqueTraders(Long uniqueTraders) {
        this.uniqueTraders = uniqueTraders;
    }

    @Override
    public String toString() {
        return "InstrumentTrendResponse{" +
                "date=" + date +
                ", ticker='" + ticker + '\'' +
                ", instrumentName='" + instrumentName + '\'' +
                ", assetClass='" + assetClass + '\'' +
                ", tradeCount=" + tradeCount +
                ", totalQuantity=" + totalQuantity +
                ", totalValue=" + totalValue +
                ", averagePrice=" + averagePrice +
                ", uniqueTraders=" + uniqueTraders +
                '}';
    }
}
