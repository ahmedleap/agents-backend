package com.agentsbackend.services;

import com.agentsbackend.DTO.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID clientId, String period);
}