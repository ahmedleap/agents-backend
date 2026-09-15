package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "instrument_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @Column(name = "holding_id", columnDefinition = "UUID")
    private UUID holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6, columnDefinition = "NUMERIC(18,6) CHECK (quantity >= 0)")
    private BigDecimal quantity;

    @Column(name = "average_cost_basis", nullable = false, precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (average_cost_basis >= 0)")
    private BigDecimal averageCostBasis;
}
