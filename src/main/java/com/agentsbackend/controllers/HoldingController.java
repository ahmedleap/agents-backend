package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
import com.agentsbackend.DTO.requests.GetHoldingRequestDTO;
import com.agentsbackend.DTO.response.CreateHoldingResponseDTO;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.services.HoldingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;




@RestController 
@RequestMapping ("/api/holding")
public class HoldingController {
    
    private final HoldingService holdingService;
    
    public HoldingController(HoldingService holdingService){
        this.holdingService = holdingService;
    }
    
    @PostMapping("/createHolding")
    public ResponseEntity<CreateHoldingResponseDTO> createHolding(
            @Valid @RequestBody CreateHoldingRequestDTO request) {
    
        Holding saved = holdingService.createHoldingFromDTO(request);
        CreateHoldingResponseDTO response = new CreateHoldingResponseDTO(saved.getHoldingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{holdingId}")
    public ResponseEntity <GetHoldingResponseDTO> getHolding(
            @PathVariable UUID holdingId) {
        
        Holding holding = holdingService.getHolding(holdingId);

        // TODO: Verify current user owns this holding
        // if (!holding.getAccount().getOwnerId().equals(getCurrentUserId())) {
        //     throw new AccessDeniedException("You don't own this holding");
        // }
        
        GetHoldingResponseDTO response = new GetHoldingResponseDTO(
            holding.getHoldingId().toString(),
            holding.getAccount().getAccountId().toString(),
            holding.getInstrument().getInstrumentId().toString(),
            holding.getQuantity(),
            holding.getAverageCostBasis()
        );

    return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
}
