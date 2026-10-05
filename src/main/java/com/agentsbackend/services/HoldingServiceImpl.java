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
        // Fetch all holdings for the account and convert each to DTO format
        java.util.List<Holding> holdings = holdingRepository.findByAccountId(accountId);
        return holdings.stream()
            .map(holding -> holdingToDTO(holding))
            .toList();
    }

    @Override
    public GetHoldingResponseDTO getOneHoldingDTO(UUID accountId, UUID holdingId){
        Holding holding = holdingRepository.findOneHolding(holdingId, accountId)
            .orElseThrow(() -> new HoldingNotFoundException(holdingId));
        return holdingToDTO(holding);
    }

    /**
     * Converts a Holding entity to a response DTO with calculated gain/loss metrics.
     * This method enriches the raw holding data with market prices and derived financial metrics.
     * 
     * @param holding the holding entity to convert
     * @return DTO containing holding details with calculated values and gain/loss
     */
    private GetHoldingResponseDTO holdingToDTO(Holding holding){
        // Fetch the current market price for this instrument
        BigDecimal currentPrice = getCurrentPrice(holding.getInstrument().getInstrumentId());
        
        // Calculate current market value: quantity * current price
        BigDecimal currentValue = holding.getQuantity().multiply(currentPrice);
        
        // Calculate total cost basis: quantity * average price paid for all shares
        BigDecimal totalCostBasis = holding.getQuantity().multiply(holding.getAverageCostBasis());
        
        // Calculate unrealized gain/loss in dollars: current value - cost basis
        BigDecimal gainLossDollars = currentValue.subtract(totalCostBasis);
        
        // Calculate unrealized gain/loss as a percentage for easy performance comparison
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

    /**
     * Calculates gain/loss percentage to provide a normalized performance metric.
     * Uses HALF_UP rounding to match industry-standard financial calculations.
     * 
     * @param gainLossDollars the absolute gain/loss amount in dollars
     * @param totalCostBasis the total amount invested (quantity * average cost per share)
     * @return gain/loss as a percentage, or 0 if cost basis is zero (no shares owned)
     */
    private BigDecimal calculateGainLossPercent(BigDecimal gainLossDollars, BigDecimal totalCostBasis){
        // Prevent division by zero when calculating percentage returns
        // If there's no cost basis (new holding with zero shares), return 0% instead of infinity
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate percentage: (gain/loss / cost basis) * 100
            // Use 4 decimal places for precision before multiplying by 100
            return gainLossDollars.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Updates holdings when a BUY order is fulfilled.
     * Creates a new holding if this is the first purchase of an instrument,
     * or updates an existing holding by recalculating the average cost basis.
     * 
     * Average cost basis calculation: (previous total cost + new purchase cost) / total shares
     * This method is called from OrderFulfillmentService when a BUY order fills.
     * 
     * @param order the completed buy order with account and instrument information
     * @param filledPrice the actual price at which the order was filled (execution price)
     */
    @Override
    public void updateHoldingsForBuy(Order order, BigDecimal filledPrice) {
        // Check if account already holds this instrument
        Holding holding = holdingRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        if (holding == null) {
            // FIRST PURCHASE: Create new holding for this instrument
            holding = new Holding();
            holding.setHoldingId(UUID.randomUUID());
            
            // Set account reference (only ID is needed for database persistence)
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            // Set instrument reference (only ID is needed for database persistence)
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
            
            // Initialize holding with order details
            holding.setQuantity(order.getQuantity());
            // For first purchase, average cost basis equals the purchase price
            holding.setAverageCostBasis(filledPrice);
        } else {
            // ADDITIONAL PURCHASE: Recalculate average cost basis for existing holding
            // Formula: average_cost_basis = (existing shares * existing avg price + new shares * new price) / total shares
            
            // Calculate total value of existing shares at their average cost
            BigDecimal currentValue = holding.getQuantity().multiply(holding.getAverageCostBasis());
            
            // Calculate total value of newly purchased shares
            BigDecimal newSharesValue = order.getQuantity().multiply(filledPrice);
            
            // Combine existing and new purchases for total investment
            BigDecimal totalValue = currentValue.add(newSharesValue);
            
            // Calculate total quantity: existing + new shares
            BigDecimal totalQuantity = holding.getQuantity().add(order.getQuantity());
            
            // Update holding with new quantity
            holding.setQuantity(totalQuantity);
            
            // Update average cost basis: total investment / total shares
            // Use 4 decimal places for precision in cost calculations
            holding.setAverageCostBasis(totalValue.divide(totalQuantity, 4, RoundingMode.HALF_UP));
            
            // Set account and instrument references for database persistence
            Account account = new Account();
            account.setAccountId(order.getAccount().getAccountId());
            holding.setAccount(account);
            
            Instrument instrument = new Instrument();
            instrument.setInstrumentId(order.getInstrument().getInstrumentId());
            holding.setInstrument(instrument);
        }
        
        // Persist the holding (create new or update existing) to the database
        holdingRepository.save(holding);
    }
    
    /**
     * Updates holdings when a SELL order is fulfilled.
     * Reduces the holding quantity by the sold amount, or deletes the holding entirely
     * if all shares are sold (quantity reaches zero).
     * 
     * Note: Average cost basis is NOT updated on sell - it remains fixed to track
     * the original purchase cost for gain/loss calculations.
     * 
     * This method is called from OrderFulfillmentService when a SELL order fills.
     * 
     * @param order the completed sell order with account and instrument information
     */
    @Override
    public void updateHoldingsForSell(Order order) {
        // Look up the current holding for this account and instrument
        Holding holding = holdingRepository.findByAccountAndInstrument(
            order.getAccount().getAccountId(),
            order.getInstrument().getInstrumentId()
        );
        
        // Only process if the holding exists (should always exist for a valid sell order)
        if (holding != null) {
            // Calculate remaining shares after the sale
            BigDecimal newQuantity = holding.getQuantity().subtract(order.getQuantity());
            
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // DELETE CASE: All shares sold, remove holding from portfolio
                holdingRepository.deleteByAccountAndInstrument(
                    order.getAccount().getAccountId(),
                    order.getInstrument().getInstrumentId()
                );
                logger.debug("Holding deleted for account {} instrument {} (quantity 0)", 
                    order.getAccount().getAccountId(), order.getInstrument().getInstrumentId());
            } else {
                // PARTIAL SALE: Update quantity to reflect remaining shares
                // Note: Average cost basis stays the same for gain/loss tracking
                holding.setQuantity(newQuantity);
                
                Account account = new Account();
                account.setAccountId(order.getAccount().getAccountId());
                holding.setAccount(account);
                
                // Set instrument reference for database persistence
                Instrument instrument = new Instrument();
                instrument.setInstrumentId(order.getInstrument().getInstrumentId());
                holding.setInstrument(instrument);
                
                // Persist the updated holding to the database
                holdingRepository.save(holding);
            }
        }
    }
}

