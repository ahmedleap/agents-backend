package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.util.UUID;

public class Holding {

    private UUID holdingId;
    private Account account;
    private Instrument instrument;
    private UUID instrumentId;
    private BigDecimal quantity;
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

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
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
