package com.agentsbackend.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class MarketMoversResponse {
    private final List<Item> marketMovers;
    private final OffsetDateTime asOf;
    private final OffsetDateTime refreshedAt;
    private final boolean stale;

    public MarketMoversResponse(List<Item> marketMovers, OffsetDateTime asOf,
                                OffsetDateTime refreshedAt, boolean stale) {
        this.marketMovers = marketMovers;
        this.asOf = asOf;
        this.refreshedAt = refreshedAt;
        this.stale = stale;
    }

    public List<Item> getMarketMovers() { return marketMovers; }
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetDateTime getAsOf() { return asOf; }
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetDateTime getRefreshedAt() { return refreshedAt; }
    @JsonProperty("isStale")
    public boolean isStale() { return stale; }

    public static class Item {
        private final String ticker;
        private final BigDecimal price;
        private final BigDecimal changePercent;

        public Item(String ticker, BigDecimal price, BigDecimal changePercent) {
            this.ticker = ticker;
            this.price = price;
            this.changePercent = changePercent;
        }

        public String getTicker() { return ticker; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getChangePercent() { return changePercent; }
    }
}