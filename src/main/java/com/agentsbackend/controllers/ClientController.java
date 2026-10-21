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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;



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
@Tag(name = "Clients", description = "Client profile for the settings page")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Get a client profile", description = "Returns the logged-in client's profile for the settings page.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client profile returned"),
        @ApiResponse(responseCode = "403", description = "Client ID is not the logged-in client"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponseDTO> getClient(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Client identifier (must be the logged-in client)", required = true)
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
    @Operation(summary = "Update a client profile", description = "Updates one or more profile fields. Fields left out are not changed; send \"\" for middleName to clear it.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client profile updated"),
        @ApiResponse(responseCode = "400", description = "Invalid profile update or no fields sent"),
        @ApiResponse(responseCode = "403", description = "Client ID is not the logged-in client"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponseDTO> updateClient(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Client identifier (must be the logged-in client)", required = true)
            @PathVariable("clientId") UUID clientId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Profile fields to update (one or more)",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateClientRequestDTO.class))
            )
            @Valid @RequestBody UpdateClientRequestDTO request) {
        authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
        ClientResponseDTO response = clientService.updateClient(clientId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
