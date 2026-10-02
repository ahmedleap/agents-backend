package com.agentsbackend.entities;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * DimAccount represents the Account dimension from the warehouse schema.
 * Used for analytics queries against dim_accounts table.
 */
public class DimAccount {
    private UUID accountId;
    private UUID clientId;
    private String accountName;
    private BigDecimal cashBalance;
    private String status;
    private ZonedDateTime openDate;
    private ZonedDateTime extractTimestamp;

    // Constructors
    public DimAccount() {}

    public DimAccount(UUID accountId, UUID clientId, String accountName,
                     BigDecimal cashBalance, String status, ZonedDateTime openDate) {
        this.accountId = accountId;
        this.clientId = clientId;
        this.accountName = accountName;
        this.cashBalance = cashBalance;
        this.status = status;
        this.openDate = openDate;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(ZonedDateTime openDate) {
        this.openDate = openDate;
    }

    public ZonedDateTime getExtractTimestamp() {
        return extractTimestamp;
    }

    public void setExtractTimestamp(ZonedDateTime extractTimestamp) {
        this.extractTimestamp = extractTimestamp;
    }
}
