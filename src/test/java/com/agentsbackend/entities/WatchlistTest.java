package com.agentsbackend.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WatchlistTest {

    @Test
    void storesAndReturnsAllWatchlistProperties() {
        UUID watchlistId = UUID.fromString("b50e8400-e29b-41d4-a716-446655aa0001");
        UUID clientId = UUID.fromString("850e8400-e29b-41d4-a716-446655770001");
        UUID instrumentId = UUID.fromString("650e8400-e29b-41d4-a716-446655550001");
        LocalDateTime addedAt = LocalDateTime.parse("2026-09-21T12:00:00");

        Watchlist watchlist = new Watchlist();
        watchlist.setWatchlistId(watchlistId);
        watchlist.setClientId(clientId);
        watchlist.setInstrumentId(instrumentId);
        watchlist.setTicker("AAPL");
        watchlist.setInstrumentName("Apple Inc.");
        watchlist.setAddedAt(addedAt);

        assertEquals(watchlistId, watchlist.getWatchlistId());
        assertEquals(clientId, watchlist.getClientId());
        assertEquals(instrumentId, watchlist.getInstrumentId());
        assertEquals("AAPL", watchlist.getTicker());
        assertEquals("Apple Inc.", watchlist.getInstrumentName());
        assertEquals(addedAt, watchlist.getAddedAt());
    }
}