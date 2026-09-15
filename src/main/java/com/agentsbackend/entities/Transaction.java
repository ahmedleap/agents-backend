package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.agentsbackend.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @Column(name = "transaction_id", columnDefinition = "UUID")
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "txn_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType txnType;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2, columnDefinition = "NUMERIC(18,2) CHECK (amount > 0)")
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
