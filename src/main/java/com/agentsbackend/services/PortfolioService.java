package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import java.util.UUID;

public interface PortfolioService {
    GetAccountPortfolioResponseDTO getPortfolioByAccountId(UUID accountId);
}
