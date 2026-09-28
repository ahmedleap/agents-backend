package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import java.math.BigDecimal;

/**
 * Service for managing holdings operations including buy and sell order processing.
 */
public interface HoldingsService {
    
    /**
     * Update holdings for a BUY order - increase quantity and recalculate average cost basis
     */
    void updateHoldingsForBuy(Order order, BigDecimal filledPrice);
    
    /**
     * Update holdings for a SELL order - decrease quantity or delete if quantity becomes zero
     */
    void updateHoldingsForSell(Order order);
}
