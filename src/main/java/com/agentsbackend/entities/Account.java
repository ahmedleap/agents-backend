package com.agentsbackend.entities;

import com.agentsbackend.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Account {

    private UUID accountId;
    private Client client;
    private UUID clientId;
    private String name;
    private BigDecimal cashBalance;
    private AccountStatus status;
    private LocalDateTime openDate;
    private List<Order> orders;
    private List<Holding> holdings;
    private List<Transaction> transactions;
    private List<HistoricalSnapshot> snapshots;

    public Account() {
    }

    public Account(UUID accountId, Client client, String name, BigDecimal cashBalance, AccountStatus status,
                   LocalDateTime openDate, List<Order> orders, List<Holding> holdings,
                   List<Transaction> transactions, List<HistoricalSnapshot> snapshots) {
        this.accountId = accountId;
        this.client = client;
        this.name = name;
        this.cashBalance = cashBalance;
        this.status = status;
        this.openDate = openDate;
        this.orders = orders;
        this.holdings = holdings;
        this.transactions = transactions;
        this.snapshots = snapshots;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    // Retrieves the user-friendly name of this account
    public String getName() {
        return name;
    }

    // Sets the user-friendly name of this account
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<HistoricalSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<HistoricalSnapshot> snapshots) {
        this.snapshots = snapshots;
    }
}
