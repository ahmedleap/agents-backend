package com.agentsbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "instrument_id"})
})
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

    public Holding() {
    }

    public Holding(UUID holdingId, Account account, Instrument instrument,
                   BigDecimal quantity, BigDecimal averageCostBasis) {
        this.holdingId = holdingId;
        this.account = account;
        this.instrument = instrument;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
    }

    public UUID getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(UUID holdingId) {
        this.holdingId = holdingId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageCostBasis() {
        return averageCostBasis;
    }

    public void setAverageCostBasis(BigDecimal averageCostBasis) {
        this.averageCostBasis = averageCostBasis;
    }
}
