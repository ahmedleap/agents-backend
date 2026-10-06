package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Holding;
import java.util.UUID;
import com.agentsbackend.entities.Order;
import java.math.BigDecimal;

public interface HoldingService {
    java.util.List<Holding> getHoldingsByAccountId(UUID accountId);
    BigDecimal getCurrentPrice(UUID instrumentId);
    java.util.List<GetHoldingResponseDTO> getHoldingsDTOByAccountId(UUID accountId);
    GetHoldingResponseDTO getOneHoldingDTO(UUID accountId, UUID holdingId);
    /**
     * Update holdings for a BUY order - increase quantity and recalculate average cost basis
     */
    void updateHoldingsForBuy(Order order, BigDecimal filledPrice);
    
    /**
     * Update holdings for a SELL order - decrease quantity or delete if quantity becomes zero
     */
    void updateHoldingsForSell(Order order);
}
