package com.agentsbackend.controllers;


import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.services.HoldingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import java.util.List;
import java.util.UUID;




@RestController 
@RequestMapping ("/api/holding")
public class HoldingController {
    
    private final HoldingService holdingService;
    
    public HoldingController(HoldingService holdingService){
        this.holdingService = holdingService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<java.util.List<GetHoldingResponseDTO>> getHoldings(
            @PathVariable UUID accountId) {
        java.util.List<GetHoldingResponseDTO> response = holdingService.getHoldingsDTOByAccountId(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{accountId}/{holdingId}")
    public ResponseEntity<GetHoldingResponseDTO> getOneHolding(
            @PathVariable UUID accountId, 
            @PathVariable UUID holdingId){
        GetHoldingResponseDTO response = holdingService.getOneHoldingDTO(accountId, holdingId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
}
