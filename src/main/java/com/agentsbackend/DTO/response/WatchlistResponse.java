package com.agentsbackend.DTO.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WatchlistResponse(
        UUID watchlistId,
        UUID clientId,
        UUID instrumentId,
        String ticker,
        String instrumentName,
        OffsetDateTime addedAt) {
}
