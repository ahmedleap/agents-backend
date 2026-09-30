package com.agentsbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instrument_price_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"instrument_id", "timestamp"})
})
public class InstrumentPrice {

    @Id
    @Column(name = "price_history_id", columnDefinition = "UUID")
    private UUID priceHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "open", nullable = false, precision = 18, scale = 4)
    private BigDecimal open;

    @Column(name = "high", nullable = false, precision = 18, scale = 4)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 18, scale = 4)
    private BigDecimal low;

    @Column(name = "close", nullable = false, precision = 18, scale = 4)
    private BigDecimal close;

    @Column(name = "volume", nullable = false)
    private Long volume;

    public InstrumentPrice() {
    }

    public InstrumentPrice(UUID priceHistoryId, Instrument instrument, LocalDateTime timestamp, 
                          BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        this.priceHistoryId = priceHistoryId;
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public UUID getPriceHistoryId() {
        return priceHistoryId;
    }

    public void setPriceHistoryId(UUID priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }
}
