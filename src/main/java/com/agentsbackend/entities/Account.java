package com.agentsbackend.entities;

import jakarta.persistence.*;
import com.agentsbackend.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "account_id", columnDefinition = "UUID")
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "client_id", insertable = false, updatable = false, columnDefinition = "UUID")
    private UUID clientId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "cash_balance", nullable = false, precision = 18, scale = 2, columnDefinition = "NUMERIC(18,2) DEFAULT 0 CHECK (cash_balance >= 0)")
    private BigDecimal cashBalance;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "open_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime openDate;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holding> holdings;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
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
