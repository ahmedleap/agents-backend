package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.AddWatchlistRequest;
import com.agentsbackend.DTO.response.WatchlistResponse;

import java.util.List;
import java.util.UUID;

public interface WatchlistService {

    WatchlistResponse add(AddWatchlistRequest request);

    List<WatchlistResponse> findByClientId(UUID clientId);

    boolean delete(UUID clientId, UUID instrumentId);
}
