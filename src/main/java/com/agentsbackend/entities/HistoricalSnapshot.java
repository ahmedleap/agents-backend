package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "historical_snapshot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "snapshot_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
