package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.AddWatchlistRequest;
import com.agentsbackend.DTO.response.WatchlistResponse;
import com.agentsbackend.entities.Watchlist;
import com.agentsbackend.repos.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistServiceImpl(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    @Override
    public WatchlistResponse add(AddWatchlistRequest request) {
        Watchlist watchlist = new Watchlist();
        watchlist.setWatchlistId(UUID.randomUUID());
        watchlist.setClientId(request.clientId());
        watchlist.setInstrumentId(request.instrumentId());
        watchlistRepository.add(watchlist);

        return watchlistRepository.findByClientAndInstrument(request.clientId(), request.instrumentId())
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("Watchlist entry could not be created"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistResponse> findByClientId(UUID clientId) {
        return watchlistRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public boolean delete(UUID clientId, UUID instrumentId) {
        return watchlistRepository.delete(clientId, instrumentId) > 0;
    }

    private WatchlistResponse toResponse(Watchlist watchlist) {
        return new WatchlistResponse(
                watchlist.getWatchlistId(),
                watchlist.getClientId(),
                watchlist.getInstrumentId(),
                watchlist.getTicker(),
                watchlist.getInstrumentName(),
                watchlist.getAddedAt());
    }
}
