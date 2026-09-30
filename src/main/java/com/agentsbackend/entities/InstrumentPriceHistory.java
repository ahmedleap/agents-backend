package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InstrumentPriceHistory {

    private UUID priceHistoryId;
    private Instrument instrument;
    private LocalDateTime timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Integer volume;

    public InstrumentPriceHistory() {
    }

    public InstrumentPriceHistory(UUID priceHistoryId, Instrument instrument, LocalDateTime timestamp,
                                  BigDecimal open, BigDecimal high, BigDecimal low,
                                  BigDecimal close, Integer volume) {
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

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }
}
