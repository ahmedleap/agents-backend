package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO;
import com.agentsbackend.services.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;



/**
 * REST API controller for portfolio management and analysis.
 * Provides endpoints to retrieve portfolio data including:
 * - Individual account portfolios with holdings and performance metrics
 * - Entire client portfolios across all accounts (aggregated)
 * - Portfolio allocation breakdowns by industry and asset class
 * 
 * All endpoints enforce authorization checks to ensure clients can only access their own portfolios.
 */
@RestController 
@RequestMapping("/api/portfolio/v1")
public class PortfolioController {
    private final PortfolioService portfolioService;
    
    public PortfolioController(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{clientId}/{accountId}")
    /**
     * Retrieves portfolio for a single account including all holdings and aggregated metrics.
     * Validates that the account belongs to the specified client before returning data.
     * 
     * @param clientId the client ID (owner of the account)
     * @param accountId the account ID to retrieve portfolio for
     * @return account portfolio with all holdings, total value, and gain/loss metrics
     */
    public ResponseEntity<GetAccountPortfolioResponseDTO> getPortfolioByAccountId(
            @PathVariable UUID clientId,
            @PathVariable UUID accountId) {
        GetAccountPortfolioResponseDTO response = portfolioService.getPortfolioByAccountId(clientId, accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{clientId}")
    /**
     * Retrieves aggregated portfolio across all accounts owned by a client.
     * Combines all holdings from all accounts and calculates total portfolio metrics.
     * 
     * @param clientId the client ID to retrieve complete portfolio for
     * @return entire portfolio with all accounts and aggregated metrics
     */
    public ResponseEntity<EntirePortfolioResponseDTO> getEntirePortfolio(
            @PathVariable UUID clientId) {
        EntirePortfolioResponseDTO response = portfolioService.getEntirePortfolioByClientId(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/allocation/{clientId}")
    /**
     * Retrieves portfolio allocation breakdown by industry and asset class for all client accounts.
     * Calculates percentage allocation to help identify concentration and diversification.
     * 
     * @param clientId the client ID to calculate allocation for
     * @return allocation breakdown by industry and asset class with percentages
     */
    public ResponseEntity<GetAllocationResponseDTO> getPortfolioAllocation(
            @PathVariable UUID clientId) {
        GetAllocationResponseDTO response = portfolioService.getPortfolioAllocation(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/allocation/{clientId}/{accountId}")
    /**
     * Retrieves allocation breakdown for a specific account.
     * Shows how portfolio is distributed across industries and asset classes.
     * Validates that the account belongs to the specified client.
     * 
     * @param clientId the client ID (owner of the account)
     * @param accountId the account ID to calculate allocation for
     * @return allocation breakdown by industry and asset class for the account
     */
    public ResponseEntity<GetAllocationResponseDTO> getAccountAllocation(
            @PathVariable UUID clientId,
            @PathVariable UUID accountId) {
        GetAllocationResponseDTO response = portfolioService.getAccountAllocation(clientId, accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
