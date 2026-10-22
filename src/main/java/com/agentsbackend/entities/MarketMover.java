package com.agentsbackend.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MarketMover {
    private Integer rank;
    private UUID instrumentId;
    private UUID snapshotId;
    private String ticker;
    private BigDecimal price;
    private BigDecimal changePercent;
    private String sourceMarket;
    private OffsetDateTime sourceAsOf;
    private OffsetDateTime refreshedAt;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
    public UUID getSnapshotId() { return snapshotId; }
    public void setSnapshotId(UUID snapshotId) { this.snapshotId = snapshotId; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
    public String getSourceMarket() { return sourceMarket; }
    public void setSourceMarket(String sourceMarket) { this.sourceMarket = sourceMarket; }
    public OffsetDateTime getSourceAsOf() { return sourceAsOf; }
    public void setSourceAsOf(OffsetDateTime sourceAsOf) { this.sourceAsOf = sourceAsOf; }
    public OffsetDateTime getRefreshedAt() { return refreshedAt; }
    public void setRefreshedAt(OffsetDateTime refreshedAt) { this.refreshedAt = refreshedAt; }
}