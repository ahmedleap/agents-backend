package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping("/api/portfolio")
public class PortfolioController {
    
}
