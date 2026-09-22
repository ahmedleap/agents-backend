package com.agentsbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "historical_snapshot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "snapshot_date"})
})
public class HistoricalSnapshot {

    @Id
    @Column(name = "snapshot_id", columnDefinition = "UUID")
    private UUID snapshotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "cash_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "holdings_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal holdingsValue;

    @Column(name = "total_value", nullable = false, precision = 18, scale = 2)
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
