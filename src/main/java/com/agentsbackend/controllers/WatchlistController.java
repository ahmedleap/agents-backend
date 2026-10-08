package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AddWatchlistRequest;
import com.agentsbackend.DTO.response.WatchlistResponse;
import com.agentsbackend.services.WatchlistService;
import com.agentsbackend.services.MissionAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

	private final WatchlistService watchlistService;
	private final MissionAuthorizationService authorizationService;

	public WatchlistController(WatchlistService watchlistService, MissionAuthorizationService authorizationService) {
		this.watchlistService = watchlistService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public ResponseEntity<WatchlistResponse> add(@AuthenticationPrincipal Jwt jwt,
	                                             @RequestBody AddWatchlistRequest request) {
		authorizationService.requireOwnClient(authorizationService.clientId(jwt), request.clientId());
		return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.add(request));
	}

	@GetMapping("/{clientId}")
	public ResponseEntity<List<WatchlistResponse>> findByClientId(@AuthenticationPrincipal Jwt jwt,
	                                                             @PathVariable("clientId") UUID clientId) {
		authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
		return ResponseEntity.ok(watchlistService.findByClientId(clientId));
	}

	@DeleteMapping("/{clientId}/{instrumentId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
	                                   @PathVariable("clientId") UUID clientId,
									   @PathVariable("instrumentId") UUID instrumentId) {
		authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
		if (!watchlistService.delete(clientId, instrumentId)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
}