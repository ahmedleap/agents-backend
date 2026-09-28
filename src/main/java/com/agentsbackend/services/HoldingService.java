package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Holding;
import java.util.UUID;
import java.math.BigDecimal;

public interface HoldingService {
    java.util.List<Holding> getHoldingsByAccountId(UUID accountId);
    BigDecimal getCurrentPrice(UUID instrumentId);
    java.util.List<GetHoldingResponseDTO> getHoldingsDTOByAccountId(UUID accountId);
    GetHoldingResponseDTO getOneHoldingDTO(UUID accountId, UUID holdingId);
}
