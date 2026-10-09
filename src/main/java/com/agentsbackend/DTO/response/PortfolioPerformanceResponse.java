package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Portfolio performance analytics response DTO.
 * Contains aggregated portfolio metrics for a given account over time.
 */
public class PortfolioPerformanceResponse {
    private UUID accountId;
    private LocalDate snapshotDate;
    private BigDecimal cashBalance;
    private BigDecimal holdingsValue;
    private BigDecimal totalValue;
    private BigDecimal dayChangeAmount;
    private BigDecimal dayChangePercent;
    private BigDecimal monthChangeAmount;
    private BigDecimal monthChangePercent;
    private String accountStatus;

    public PortfolioPerformanceResponse() {}

    public PortfolioPerformanceResponse(UUID accountId, LocalDate snapshotDate,
                                       BigDecimal cashBalance, BigDecimal holdingsValue,
                                       BigDecimal totalValue) {
        this.accountId = accountId;
        this.snapshotDate = snapshotDate;
        this.cashBalance = cashBalance;
        this.holdingsValue = holdingsValue;
        this.totalValue = totalValue;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getHoldingsValue() {
        return holdingsValue;
    }

    public void setHoldingsValue(BigDecimal holdingsValue) {
        this.holdingsValue = holdingsValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getDayChangeAmount() {
        return dayChangeAmount;
    }

    public void setDayChangeAmount(BigDecimal dayChangeAmount) {
        this.dayChangeAmount = dayChangeAmount;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public BigDecimal getMonthChangeAmount() {
        return monthChangeAmount;
    }

    public void setMonthChangeAmount(BigDecimal monthChangeAmount) {
        this.monthChangeAmount = monthChangeAmount;
    }

    public BigDecimal getMonthChangePercent() {
        return monthChangePercent;
    }

    public void setMonthChangePercent(BigDecimal monthChangePercent) {
        this.monthChangePercent = monthChangePercent;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
