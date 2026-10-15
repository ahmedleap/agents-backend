package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.DashboardResponse;
import com.agentsbackend.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam UUID clientId,
            @RequestParam(defaultValue = "30D") String period) {
        return ResponseEntity.ok(dashboardService.getDashboard(clientId, period));
    }
}