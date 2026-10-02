package com.agentsbackend.entities;

import com.agentsbackend.enums.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Transaction {

    private UUID transactionId;
    private Account account;
    private TransactionType txnType;
    private BigDecimal amount;
    private OffsetDateTime createdAt;

    public Transaction() {
    }

    public Transaction(UUID transactionId, Account account, TransactionType txnType,
                      BigDecimal amount, OffsetDateTime createdAt) {
        this.transactionId = transactionId;
        this.account = account;
        this.txnType = txnType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionType getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionType txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
