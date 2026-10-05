package com.agentsbackend.services;

import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Order;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.exceptions.HoldingNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
@Transactional
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private static final Logger logger = LoggerFactory.getLogger(HoldingServiceImpl.class);
    
    

    public HoldingServiceImpl(HoldingRepository holdingRepository,
                             AccountRepository accountRepository,
                             InstrumentRepository instrumentRepository) {
        this.holdingRepository = holdingRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
    }
    
    @Override
    public java.util.List<Holding> getHoldingsByAccountId(UUID accountId){
        return holdingRepository.findByAccountId(accountId);
    }

    @Override
    public BigDecimal getCurrentPrice(UUID instrumentId){
        return instrumentRepository.findById(instrumentId)
            .map(instrument -> instrument.getMidPrice())
            .orElse(BigDecimal.ZERO);
    }

    @Override
    public java.util.List<GetHoldingResponseDTO> getHoldingsDTOByAccountId(UUID accountId){
        java.util.List<Holding> holdings = holdingRepository.findByAccountId(accountId);
        return holdings.stream()
            .map(this::holdingToDTO)
            .toList();
    }

    @Override
    public GetHoldingResponseDTO getOneHoldingDTO(UUID accountId, UUID holdingId){
        Holding holding = holdingRepository.findOneHolding(holdingId, accountId)
            .orElseThrow(() -> new HoldingNotFoundException(holdingId));
        return holdingToDTO(holding);
    }

    private GetHoldingResponseDTO holdingToDTO(Holding holding){
        BigDecimal currentPrice = getCurrentPrice(holding.getInstrument().getInstrumentId());
        BigDecimal currentValue = holding.getQuantity().multiply(currentPrice);
        BigDecimal totalCostBasis = holding.getQuantity().multiply(holding.getAverageCostBasis());
        BigDecimal gainLossDollars = currentValue.subtract(totalCostBasis);
        BigDecimal gainLossPercent = calculateGainLossPercent(gainLossDollars, totalCostBasis);

        return new GetHoldingResponseDTO(
            holding.getHoldingId().toString(),
            holding.getAccount().getAccountId().toString(),
            holding.getInstrument().getInstrumentId().toString(),
            holding.getInstrument().getTicker(),
            holding.getInstrument().getName(),
            holding.getQuantity(),
            holding.getAverageCostBasis(),
            currentPrice,
            currentValue,
            gainLossDollars,
            gainLossPercent
        );
    }

    private BigDecimal calculateGainLossPercent(BigDecimal gainLossDollars, BigDecimal totalCostBasis){
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            return gainLossDollars.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void updateHoldingsForBuy(Order order, BigDecimal filledPrice) {
        Holding holding = holdingRepository.findByAccountAndInstrument(
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
            holding.setAverageCostBasis(totalValue.divide(totalQuantity, 4, RoundingMode.HALF_UP));
            
            // Ensure Account and Instrument IDs are properly set before saving
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
        }
        
        holdingRepository.save(holding);
    }
    
    /**
     * Update holdings for a SELL order - decrease quantity
     */
    @Override
    public void updateHoldingsForSell(Order order) {
        Holding holding = holdingRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding != null) {
            BigDecimal newQuantity = holding.getQuantity().subtract(order.getQuantity());
            
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // Delete holding if quantity becomes zero
                holdingRepository.deleteByAccountAndInstrument(
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
                
                holdingRepository.save(holding);
            }
        }
    }
}

