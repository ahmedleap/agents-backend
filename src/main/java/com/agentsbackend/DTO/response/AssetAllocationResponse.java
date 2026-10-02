package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;

/**
 * Asset allocation analytics response DTO.
 * Contains asset class and instrument allocation metrics for a portfolio.
 */
public class AssetAllocationResponse {
    private UUID accountId;
    private String assetClass;
    private String ticker;
    private String instrumentName;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal percentOfPortfolio;
    private BigDecimal costBasis;
    private BigDecimal unrealizedGain;
    private BigDecimal unrealizedGainPercent;

    public AssetAllocationResponse() {}

    public AssetAllocationResponse(UUID accountId, String assetClass, String ticker,
                                  String instrumentName, BigDecimal quantity,
                                  BigDecimal currentValue) {
        this.accountId = accountId;
        this.assetClass = assetClass;
        this.ticker = ticker;
        this.instrumentName = instrumentName;
        this.quantity = quantity;
        this.currentValue = currentValue;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getPercentOfPortfolio() {
        return percentOfPortfolio;
    }

    public void setPercentOfPortfolio(BigDecimal percentOfPortfolio) {
        this.percentOfPortfolio = percentOfPortfolio;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public void setCostBasis(BigDecimal costBasis) {
        this.costBasis = costBasis;
    }

    public BigDecimal getUnrealizedGain() {
        return unrealizedGain;
    }

    public void setUnrealizedGain(BigDecimal unrealizedGain) {
        this.unrealizedGain = unrealizedGain;
    }

    public BigDecimal getUnrealizedGainPercent() {
        return unrealizedGainPercent;
    }

    public void setUnrealizedGainPercent(BigDecimal unrealizedGainPercent) {
        this.unrealizedGainPercent = unrealizedGainPercent;
    }
}
