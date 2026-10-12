package com.agentsbackend.entities;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * FactHistoricalSnapshot represents the Portfolio Snapshot fact from the warehouse schema.
 * Used for analytics queries against fact_historical_snapshots table.
 * Grain: one row per account per business day.
 */
public class FactHistoricalSnapshot {
    private UUID snapshotId;
    private UUID accountId;
    private LocalDate snapshotDate;
    private BigDecimal cashBalance;
    private BigDecimal holdingsValue;
    private BigDecimal totalValue;
    private ZonedDateTime extractTimestamp;

    // Constructors
    public FactHistoricalSnapshot() {}

    public FactHistoricalSnapshot(UUID snapshotId, UUID accountId, LocalDate snapshotDate,
                                 BigDecimal cashBalance, BigDecimal holdingsValue, BigDecimal totalValue) {
        this.snapshotId = snapshotId;
        this.accountId = accountId;
        this.snapshotDate = snapshotDate;
        this.cashBalance = cashBalance;
        this.holdingsValue = holdingsValue;
        this.totalValue = totalValue;
    }

    // Getters and Setters
    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        this.snapshotId = snapshotId;
    }

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

    public ZonedDateTime getExtractTimestamp() {
        return extractTimestamp;
    }

    public void setExtractTimestamp(ZonedDateTime extractTimestamp) {
        this.extractTimestamp = extractTimestamp;
    }
}
