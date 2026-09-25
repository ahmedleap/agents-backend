package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
import com.agentsbackend.entities.Holding;
import java.util.UUID;

public interface HoldingService {

    Holding createHoldingFromDTO(CreateHoldingRequestDTO request);
    Holding createHolding(Holding holding);
    Holding getHolding(UUID holdingId);
}
