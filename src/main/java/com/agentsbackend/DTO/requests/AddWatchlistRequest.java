package com.agentsbackend.DTO.requests;

import java.util.UUID;

public record AddWatchlistRequest(UUID clientId, UUID instrumentId) {
}
