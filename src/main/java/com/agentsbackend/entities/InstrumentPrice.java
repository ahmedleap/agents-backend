package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InstrumentPrice {

    private UUID priceId;
    private Instrument instrument;
    private BigDecimal price;
    private LocalDateTime asOf;

    public InstrumentPrice() {
    }

    public InstrumentPrice(UUID priceId, Instrument instrument, BigDecimal price, LocalDateTime asOf) {
        this.priceId = priceId;
        this.instrument = instrument;
        this.price = price;
        this.asOf = asOf;
    }

    public UUID getPriceId() {
        return priceId;
    }

    public void setPriceId(UUID priceId) {
        this.priceId = priceId;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }
}
