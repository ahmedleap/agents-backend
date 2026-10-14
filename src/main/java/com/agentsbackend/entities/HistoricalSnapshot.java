package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class HistoricalSnapshot {

    private UUID snapshotId;
    private Account account;
    private LocalDate snapshotDate;
    private BigDecimal cashBalance;
    private BigDecimal holdingsValue;
    private BigDecimal totalValue;

    public HistoricalSnapshot() {
    }

    public HistoricalSnapshot(UUID snapshotId, Account account, LocalDate snapshotDate,
                               BigDecimal cashBalance, BigDecimal holdingsValue, BigDecimal totalValue) {
        this.snapshotId = snapshotId;
        this.account = account;
        this.snapshotDate = snapshotDate;
        this.cashBalance = cashBalance;
        this.holdingsValue = holdingsValue;
        this.totalValue = totalValue;
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
}
