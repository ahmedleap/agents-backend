package com.agentsbackend.services;

import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO;
import java.util.UUID;

public interface PortfolioService {
    GetAccountPortfolioResponseDTO getPortfolioByAccountId(UUID clientId, UUID accountId);
    EntirePortfolioResponseDTO getEntirePortfolioByClientId(UUID clientId);
    GetAllocationResponseDTO getPortfolioAllocation(UUID clientId);
}
