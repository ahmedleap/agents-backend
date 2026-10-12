package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Trading statistics analytics response DTO.
 * Contains aggregated trading metrics and statistics.
 */
public class TradingStatisticsResponse {
    private UUID accountId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalTrades;
    private Long buyTrades;
    private Long sellTrades;
    private BigDecimal totalVolume;
    private BigDecimal totalVolumeValue;
    private BigDecimal winRate;
    private BigDecimal lossRate;
    private BigDecimal profitFactor;
    private BigDecimal averageWin;
    private BigDecimal averageLoss;
    private BigDecimal largestWin;
    private BigDecimal largestLoss;
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal cumulativeReturn;

    public TradingStatisticsResponse() {}

    public TradingStatisticsResponse(UUID accountId, LocalDate startDate, LocalDate endDate,
                                    Long totalTrades) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalTrades = totalTrades;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(Long totalTrades) {
        this.totalTrades = totalTrades;
    }

    public Long getBuyTrades() {
        return buyTrades;
    }

    public void setBuyTrades(Long buyTrades) {
        this.buyTrades = buyTrades;
    }

    public Long getSellTrades() {
        return sellTrades;
    }

    public void setSellTrades(Long sellTrades) {
        this.sellTrades = sellTrades;
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

    public BigDecimal getWinRate() {
        return winRate;
    }

    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }

    public BigDecimal getLossRate() {
        return lossRate;
    }

    public void setLossRate(BigDecimal lossRate) {
        this.lossRate = lossRate;
    }

    public BigDecimal getProfitFactor() {
        return profitFactor;
    }

    public void setProfitFactor(BigDecimal profitFactor) {
        this.profitFactor = profitFactor;
    }

    public BigDecimal getAverageWin() {
        return averageWin;
    }

    public void setAverageWin(BigDecimal averageWin) {
        this.averageWin = averageWin;
    }

    public BigDecimal getAverageLoss() {
        return averageLoss;
    }

    public void setAverageLoss(BigDecimal averageLoss) {
        this.averageLoss = averageLoss;
    }

    public BigDecimal getLargestWin() {
        return largestWin;
    }

    public void setLargestWin(BigDecimal largestWin) {
        this.largestWin = largestWin;
    }

    public BigDecimal getLargestLoss() {
        return largestLoss;
    }

    public void setLargestLoss(BigDecimal largestLoss) {
        this.largestLoss = largestLoss;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(BigDecimal sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public BigDecimal getCumulativeReturn() {
        return cumulativeReturn;
    }

    public void setCumulativeReturn(BigDecimal cumulativeReturn) {
        this.cumulativeReturn = cumulativeReturn;
    }
}
