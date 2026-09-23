package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AddWatchlistRequest;
import com.agentsbackend.DTO.response.WatchlistResponse;
import com.agentsbackend.services.WatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

	private final WatchlistService watchlistService;

	public WatchlistController(WatchlistService watchlistService) {
		this.watchlistService = watchlistService;
	}

	@PostMapping
	public ResponseEntity<WatchlistResponse> add(@RequestBody AddWatchlistRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.add(request));
	}

	@GetMapping("/{clientId}")
	public ResponseEntity<List<WatchlistResponse>> findByClientId(@PathVariable("clientId") UUID clientId) {
		return ResponseEntity.ok(watchlistService.findByClientId(clientId));
	}

@DeleteMapping("/{clientId}/{instrumentId}")
public ResponseEntity<Void> delete(@PathVariable("clientId") UUID clientId,
                                   @PathVariable("instrumentId") UUID instrumentId) {
    if (!watchlistService.delete(clientId, instrumentId)) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
}
}
