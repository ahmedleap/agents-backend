package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import java.util.List;
import java.util.UUID;

public interface PortfolioService {
    List<GetHoldingResponseDTO> getPortfolioByAccountId(UUID accountId);
}
