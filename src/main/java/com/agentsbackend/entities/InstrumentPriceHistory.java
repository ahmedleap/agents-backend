package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class InstrumentPriceHistory {

    private UUID priceHistoryId;
    private UUID instrumentId;
    private OffsetDateTime timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;

    public InstrumentPriceHistory() {
    }

    public InstrumentPriceHistory(UUID priceHistoryId, UUID instrumentId, OffsetDateTime timestamp,
                                  BigDecimal open, BigDecimal high, BigDecimal low,
                                  BigDecimal close, Long volume) {
        this.priceHistoryId = priceHistoryId;
        this.instrumentId = instrumentId;
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

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
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
