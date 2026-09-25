package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
import com.agentsbackend.entities.Holding;
import java.util.UUID;
import java.math.BigDecimal;

public interface HoldingService {

    Holding createHoldingFromDTO(CreateHoldingRequestDTO request);
    Holding createHolding(Holding holding);
    java.util.List<Holding> getHoldingsByAccountId(UUID accountId);
    BigDecimal getCurrentPrice(UUID instrumentId);
}
