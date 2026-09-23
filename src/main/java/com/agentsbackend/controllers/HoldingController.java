package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
import com.agentsbackend.DTO.response.CreateHoldingResponseDTO;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.services.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@RequestMapping ("/api/holding")
public class HoldingController {
    
    @Autowired 
    private final HoldingService holdingService;
    
    public HoldingController(HoldingService holdingService){
        this.holdingService = holdingService;
    }
    
    @PostMapping("/createHolding")
    public ResponseEntity <CreateHoldingResponseDTO> createHolding(
            @Valid @RequestBody CreateHoldingRequestDTO request ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .build(holdingService.createHolding(request));
    }
    
}
