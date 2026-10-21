package com.agentsbackend.controllers;


import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.services.HoldingService;
import com.agentsbackend.services.MissionAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;



/**
 * REST API controller for managing holding queries.
 * Provides endpoints to retrieve holdings and their performance metrics (gains/losses)
 * for individual accounts. All endpoints return holdings with current market values
 * and unrealized gain/loss calculations.
 */
@RestController 
@RequestMapping ("/api/holding/v1")
@Tag(name = "Holdings", description = "Account holdings and unrealized performance")
@SecurityRequirement(name = "bearerAuth")
public class HoldingController {
    
    private final HoldingService holdingService;
    private final MissionAuthorizationService authorizationService;
    
    public HoldingController(HoldingService holdingService,
                             MissionAuthorizationService authorizationService){
        this.holdingService = holdingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{accountId}")
    /**
     * Retrieves all holdings for an account with current market values and gain/loss metrics.
     * 
     * @param accountId the account ID to retrieve holdings for
     * @return list of holdings with calculated market values and unrealized gains/losses
     */
        @Operation(summary = "List account holdings", description = "Returns holdings with current market values and unrealized gain/loss metrics.")
        @ApiResponse(responseCode = "200", description = "Holdings returned")
        public ResponseEntity<java.util.List<GetHoldingResponseDTO>> getHoldings(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId) {
        authorizationService.requireOwnAccount(authorizationService.clientId(jwt), accountId);
        java.util.List<GetHoldingResponseDTO> response = holdingService.getHoldingsDTOByAccountId(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{accountId}/{holdingId}")
    /**
     * Retrieves a single holding with detailed market value and gain/loss information.
     * 
     * @param accountId the account ID that owns the holding
     * @param holdingId the holding ID to retrieve
     * @return a single holding with calculated metrics, or 404 if not found
     */
    @Operation(summary = "Get a holding", description = "Returns one holding with current market value and unrealized gain/loss metrics.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Holding returned"),
        @ApiResponse(responseCode = "404", description = "Holding not found")
    })
    public ResponseEntity<GetHoldingResponseDTO> getOneHolding(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @Parameter(description = "Holding identifier", required = true)
            @PathVariable("holdingId") UUID holdingId){
        authorizationService.requireOwnAccount(authorizationService.clientId(jwt), accountId);
        GetHoldingResponseDTO response = holdingService.getOneHoldingDTO(accountId, holdingId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
}
