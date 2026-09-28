package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.services.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController 
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;
    
    public PortfolioController(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }
}
