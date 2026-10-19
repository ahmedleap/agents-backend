package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.DashboardResponse;
import com.agentsbackend.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.agentsbackend.services.MissionAuthorizationService;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final MissionAuthorizationService authorizationService;

    public DashboardController(DashboardService dashboardService,
                               MissionAuthorizationService authorizationService) {
        this.dashboardService = dashboardService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID clientId,
            @RequestParam(defaultValue = "30D") String period) {
        authorizationService.requireOwnClient(authorizationService.clientId(jwt), clientId);
        return ResponseEntity.ok(dashboardService.getDashboard(clientId, period));
    }
}