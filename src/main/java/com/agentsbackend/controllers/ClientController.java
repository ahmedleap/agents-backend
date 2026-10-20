package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.DTO.response.ClientResponseDTO;
import com.agentsbackend.services.ClientService;
import com.agentsbackend.services.MissionAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;



/**
 * REST API controller for client profile management.
 * Provides endpoints for the settings page, including:
 * - Retrieving the client's profile to fill in the settings form
 * - Updating one or more of the client's profile fields (name, risk tolerance, portfolio size range)
 *
 * All endpoints enforce authorization checks to ensure clients can only view and edit their own profile.
 */
@RestController
@RequestMapping("/api/client/v1")
public class ClientController {
    private final ClientService clientService;
    private final MissionAuthorizationService authorizationService;

    public ClientController(ClientService clientService,
                                MissionAuthorizationService authorizationService){
        this.clientService = clientService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{clientId}")
    /**
     * Retrieves a client's profile for the settings page.
     * Validates that the logged-in client is the client being retrieved before returning data.
     *
     * @param clientId the client ID to retrieve (must be the logged-in client)
     * @return the client's profile (name, email, risk tolerance, portfolio size range)
     */
    public ResponseEntity<ClientResponseDTO> getClient(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("clientId") UUID clientId) {
        authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
        ClientResponseDTO response = clientService.getClient(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{clientId}")
    /**
     * Updates one or more of a client's profile fields. Fields left out of the request are not changed.
     * Validates that the logged-in client is the client being updated before saving.
     *
     * @param clientId the client ID to update (must be the logged-in client)
     * @param request the fields to update; at least one is required
     * @return the client's profile after the update
     */
    public ResponseEntity<ClientResponseDTO> updateClient(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("clientId") UUID clientId,
            @Valid @RequestBody UpdateClientRequestDTO request) {
        authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
        ClientResponseDTO response = clientService.updateClient(clientId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
