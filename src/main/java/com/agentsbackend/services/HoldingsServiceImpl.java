package com.agentsbackend.services;

import com.agentsbackend.entities.Order;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.repos.HoldingsRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of HoldingsService for managing holdings operations.
 */
@Service
public class HoldingsServiceImpl implements HoldingsService {
    
    private static final Logger logger = LoggerFactory.getLogger(HoldingsServiceImpl.class);
    
    private final HoldingsRepository holdingsRepository;
    
    public HoldingsServiceImpl(HoldingsRepository holdingsRepository) {
        this.holdingsRepository = holdingsRepository;
    }
    
    /**
     * Update holdings for a BUY order - increase quantity
     */
    @Override
    public void updateHoldingsForBuy(Order order, BigDecimal filledPrice) {
        Holding holding = holdingsRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding == null) {
            // Create new holding
            holding = new Holding();
            holding.setHoldingId(UUID.randomUUID());
            
            // Ensure Account and Instrument objects have their IDs properly set
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
            
            holding.setQuantity(order.getQuantity());
            // Set average cost basis for new holding
            holding.setAverageCostBasis(filledPrice);
        } else {
            // Update existing holding - recalculate average cost basis
            BigDecimal currentValue = holding.getQuantity().multiply(holding.getAverageCostBasis());
            BigDecimal newSharesValue = order.getQuantity().multiply(filledPrice);
            BigDecimal totalValue = currentValue.add(newSharesValue);
            BigDecimal totalQuantity = holding.getQuantity().add(order.getQuantity());
            
            holding.setQuantity(totalQuantity);
            holding.setAverageCostBasis(totalValue.divide(totalQuantity, 4, java.math.RoundingMode.HALF_UP));
            
            // Ensure Account and Instrument IDs are properly set before saving
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
        }
        
        holdingsRepository.save(holding);
    }
    
    /**
     * Update holdings for a SELL order - decrease quantity
     */
    @Override
    public void updateHoldingsForSell(Order order) {
        Holding holding = holdingsRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding != null) {
            BigDecimal newQuantity = holding.getQuantity().subtract(order.getQuantity());
            
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // Delete holding if quantity becomes zero
                holdingsRepository.deleteByAccountAndInstrument(
                    order.getAccount().getAccountId(),
                    order.getInstrument().getInstrumentId()
                );
                logger.debug("Holding deleted for account {} instrument {} (quantity 0)", 
                    order.getAccount().getAccountId(), order.getInstrument().getInstrumentId());
            } else {
                // Update quantity - ensure Account and Instrument IDs are properly set
                holding.setQuantity(newQuantity);
                
                Account account = new Account();
                account.setAccountId(order.getAccount().getAccountId());
                holding.setAccount(account);
                
                Instrument instrument = new Instrument();
                instrument.setInstrumentId(order.getInstrument().getInstrumentId());
                holding.setInstrument(instrument);
                
                holdingsRepository.save(holding);
            }
        }
    }
}
