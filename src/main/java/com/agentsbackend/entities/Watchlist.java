package com.agentsbackend.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Watchlist {

    private UUID watchlistId;
    private UUID clientId;
    private UUID instrumentId;
    private String ticker;
    private String instrumentName;
    private OffsetDateTime addedAt;

    public Watchlist() {
    }

    public Watchlist(UUID watchlistId, UUID clientId, UUID instrumentId, OffsetDateTime addedAt) {
        this.watchlistId = watchlistId;
        this.clientId = clientId;
        this.instrumentId = instrumentId;
        this.addedAt = addedAt;
    }

    public UUID getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(UUID watchlistId) {
        this.watchlistId = watchlistId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(OffsetDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
