package com.agentsbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instrument_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"instrument_id", "as_of"})
})
public class InstrumentPrice {

    @Id
    @Column(name = "price_id", columnDefinition = "UUID")
    private UUID priceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "price", nullable = false, precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (price > 0)")
    private BigDecimal price;

    @Column(name = "as_of", nullable = false)
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
