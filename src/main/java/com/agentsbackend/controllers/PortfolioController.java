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



@RestController 
@RequestMapping("/api/portfolio/v1")
public class PortfolioController {
    private final PortfolioService portfolioService;
    
    public PortfolioController(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{clientId}/{accountId}")
    public ResponseEntity<GetAccountPortfolioResponseDTO> getPortfolioByAccountId(
            @PathVariable UUID clientId,
            @PathVariable UUID accountId) {
        GetAccountPortfolioResponseDTO response = portfolioService.getPortfolioByAccountId(clientId, accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<EntirePortfolioResponseDTO> getEntirePortfolio(
            @PathVariable UUID clientId) {
        EntirePortfolioResponseDTO response = portfolioService.getEntirePortfolioByClientId(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/allocation/{clientId}")
    public ResponseEntity<GetAllocationResponseDTO> getPortfolioAllocation(
            @PathVariable UUID clientId) {
        GetAllocationResponseDTO response = portfolioService.getPortfolioAllocation(clientId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/allocation/{clientId}/{accountId}")
    public ResponseEntity<GetAllocationResponseDTO> getAccountAllocation(
            @PathVariable UUID clientId,
            @PathVariable UUID accountId) {
        GetAllocationResponseDTO response = portfolioService.getAccountAllocation(clientId, accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
