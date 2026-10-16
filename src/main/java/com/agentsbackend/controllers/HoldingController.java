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



/**
 * REST API controller for managing holding queries.
 * Provides endpoints to retrieve holdings and their performance metrics (gains/losses)
 * for individual accounts. All endpoints return holdings with current market values
 * and unrealized gain/loss calculations.
 */
@RestController 
@RequestMapping ("/api/holding/v1")
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
    public ResponseEntity<java.util.List<GetHoldingResponseDTO>> getHoldings(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {
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
    public ResponseEntity<GetHoldingResponseDTO> getOneHolding(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId, 
            @PathVariable UUID holdingId){
        authorizationService.requireOwnAccount(authorizationService.clientId(jwt), accountId);
        GetHoldingResponseDTO response = holdingService.getOneHoldingDTO(accountId, holdingId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
}
