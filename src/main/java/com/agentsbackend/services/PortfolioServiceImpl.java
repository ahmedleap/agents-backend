package com.agentsbackend.services;

import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.exceptions.AccountNotFoundException;
import com.agentsbackend.exceptions.ClientNotFoundException;
import com.agentsbackend.exceptions.UnauthorizedAccountAccessException;
import com.agentsbackend.exceptions.HoldingNotFoundException;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.ClientRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService{
    private final AccountRepository accountRepository;
    private final HoldingService holdingService;
    private final InstrumentRepository instrumentRepository;
    private final ClientRepository clientRepository;

    public PortfolioServiceImpl(AccountRepository accountRepository,
                             HoldingService holdingService,
                             InstrumentRepository instrumentRepository,
                             ClientRepository clientRepository) {
        this.accountRepository = accountRepository;
        this.holdingService = holdingService;
        this.instrumentRepository = instrumentRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public GetAccountPortfolioResponseDTO getPortfolioByAccountId(UUID clientId, UUID accountId) {
        // Retrieve account from database, throw exception if not found
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Verify the account belongs to the client to prevent unauthorized access
        if (!account.getClient().getClientId().equals(clientId)) {
            throw new UnauthorizedAccountAccessException(clientId, accountId);
        }

        // Fetch all holdings for this account as DTOs for easy response mapping
        List<GetHoldingResponseDTO> accountHoldings;
        accountHoldings = holdingService.getHoldingsDTOByAccountId(accountId);

        // Calculate total current market value by summing all holding values
        BigDecimal totalValue = accountHoldings.stream()
            .map(GetHoldingResponseDTO::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total cost basis by multiplying quantity by average cost for each holding
        BigDecimal totalCost = accountHoldings.stream()
            .map(h -> h.getQuantity().multiply(h.getAverageCostBasis()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate gain/loss in dollars (current value - cost basis)
        BigDecimal gainLossDollars = totalValue.subtract(totalCost);
        // Calculate gain/loss as a percentage
        BigDecimal gainLossPercent = calculateGainLossPercent(gainLossDollars, totalCost);

        return new GetAccountPortfolioResponseDTO(
            account.getAccountId().toString(),
            account.getName(),
            accountHoldings,
            totalValue,
            totalCost,
            gainLossDollars,
            gainLossPercent
        );
        
    }

    @Override 
    public EntirePortfolioResponseDTO getEntirePortfolioByClientId(UUID clientId){
        // Verify client exists before proceeding
        clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));
        
        // Retrieve all accounts associated with this client
        List<Account> accounts = accountRepository.findByClientId(clientId);

        // Build portfolio response for each account, aggregating results
        List<GetAccountPortfolioResponseDTO> portfolios = accounts.stream()
            .map(account -> getPortfolioByAccountId(clientId, account.getAccountId()))
            .toList();

        // Sum total portfolio value across all accounts
        BigDecimal totalPortfolioValue = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalPortfolioValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum total cost basis across all accounts
        BigDecimal totalCostBasis = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalCostBasis)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Sum total gain/loss in dollars across all accounts
        BigDecimal totalGainLossDollars = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalGainLossDollars)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate overall gain/loss percentage across entire portfolio
        BigDecimal totalGainLossPercent = calculateGainLossPercent(totalGainLossDollars, totalCostBasis);
        
        return new EntirePortfolioResponseDTO(
            clientId.toString(),
            portfolios,
            totalPortfolioValue,
            totalCostBasis,
            totalGainLossDollars,
            totalGainLossPercent
        );

    }   

    @Override
    public GetAllocationResponseDTO getPortfolioAllocation(UUID clientId) {
        // Verify client exists before proceeding
        clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));
        
        // Get all accounts for the client
        List<Account> accounts = accountRepository.findByClientId(clientId);
        
        // Collect all holdings across all accounts for comprehensive allocation analysis
        List<Holding> allHoldings = accounts.stream()
            .flatMap(account -> holdingService.getHoldingsByAccountId(account.getAccountId()).stream())
            .toList();

        return calculateAllocation(allHoldings);
    }

    @Override
    public GetAllocationResponseDTO getAccountAllocation(UUID clientId, UUID accountId) {
        // Verify account exists and belongs to the client
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Prevent unauthorized access to accounts not owned by the client
        if (!account.getClient().getClientId().equals(clientId)) {
            throw new UnauthorizedAccountAccessException(clientId, accountId);
        }

        // Get holdings only for this specific account
        List<Holding> accountHoldings = holdingService.getHoldingsByAccountId(accountId);

        return calculateAllocation(accountHoldings);
    }

    /**
     * Calculates portfolio allocation breakdown by industry and asset class.
     * Single Responsibility: This method encapsulates all allocation calculation logic.
     * 
     * @param holdings the list of holdings to calculate allocation for
     * @return allocation response with industry and asset class breakdowns
     */
    private GetAllocationResponseDTO calculateAllocation(List<Holding> holdings) {
        // If no holdings exist, return empty allocation response
        if (holdings.isEmpty()) {
            return new GetAllocationResponseDTO(List.of(), List.of());
        }

        // Calculate total value across all holdings
        BigDecimal totalValue = calculateTotalValue(holdings);

        // If total value is zero, return empty allocation (avoids division by zero)
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return new GetAllocationResponseDTO(List.of(), List.of());
        }

        // Group by industry and calculate percentages
        List<GetAllocationResponseDTO.IndustryAllocationDTO> industryBreakdown = 
            calculateIndustryAllocation(holdings, totalValue);

        // Group by asset class and calculate percentages
        List<GetAllocationResponseDTO.AssetClassAllocationDTO> assetClassBreakdown = 
            calculateAssetClassAllocation(holdings, totalValue);

        return new GetAllocationResponseDTO(industryBreakdown, assetClassBreakdown);
    }

    /**
     * Calculates the total portfolio value from holdings.
     * Single Responsibility: Encapsulates value calculation logic.
     * 
     * @param holdings the list of holdings
     * @return total value of all holdings
     */
    private BigDecimal calculateTotalValue(List<Holding> holdings) {
        // For each holding, fetch current price and multiply by quantity, then sum all values
        return holdings.stream()
            .map(holding -> {
                BigDecimal price = instrumentRepository.findById(holding.getInstrument().getInstrumentId())
                    .map(instrument -> instrument.getMidPrice())
                    .orElse(BigDecimal.ZERO);
                return holding.getQuantity().multiply(price);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates allocation breakdown by industry.
     * Single Responsibility: Encapsulates industry grouping and percentage calculation.
     * 
     * @param holdings the list of holdings
     * @param totalValue the total portfolio value
     * @return list of industry allocations with percentages
     */
    private List<GetAllocationResponseDTO.IndustryAllocationDTO> calculateIndustryAllocation(
            List<Holding> holdings, BigDecimal totalValue) {
        // Group holdings by industry, calculating total value for each industry
        Map<String, BigDecimal> industryValues = holdings.stream()
            .collect(Collectors.groupingBy(
                holding -> holding.getInstrument().getIndustry() != null 
                    ? holding.getInstrument().getIndustry() 
                    : "Unknown",  // Default to "Unknown" if industry is not specified
                Collectors.reducing(
                    BigDecimal.ZERO,
                    holding -> {
                        BigDecimal price = instrumentRepository.findById(holding.getInstrument().getInstrumentId())
                            .map(instrument -> instrument.getMidPrice())
                            .orElse(BigDecimal.ZERO);
                        return holding.getQuantity().multiply(price);
                    },
                    BigDecimal::add
                )
            ));

        // Convert industry values to allocation DTOs with percentage calculations
        return industryValues.entrySet().stream()
            .map(entry -> {
                BigDecimal value = entry.getValue();
                // Calculate percentage: (industry value / total value) * 100
                BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                return new GetAllocationResponseDTO.IndustryAllocationDTO(
                    entry.getKey(),
                    percentage,
                    value
                );
            })
            .toList();
    }

    /**
     * Calculates allocation breakdown by asset class.
     * Single Responsibility: Encapsulates asset class grouping and percentage calculation.
     * 
     * @param holdings the list of holdings
     * @param totalValue the total portfolio value
     * @return list of asset class allocations with percentages
     */
    private List<GetAllocationResponseDTO.AssetClassAllocationDTO> calculateAssetClassAllocation(
            List<Holding> holdings, BigDecimal totalValue) {
        // Group holdings by asset class, calculating total value for each class
        Map<String, BigDecimal> assetClassValues = holdings.stream()
            .collect(Collectors.groupingBy(
                holding -> holding.getInstrument().getAssetClass().toString(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    holding -> {
                        BigDecimal price = instrumentRepository.findById(holding.getInstrument().getInstrumentId())
                            .map(instrument -> instrument.getMidPrice())
                            .orElse(BigDecimal.ZERO);
                        return holding.getQuantity().multiply(price);
                    },
                    BigDecimal::add
                )
            ));

        // Convert asset class values to allocation DTOs with percentage calculations
        return assetClassValues.entrySet().stream()
            .map(entry -> {
                BigDecimal value = entry.getValue();
                // Calculate percentage: (asset class value / total value) * 100
                BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                return new GetAllocationResponseDTO.AssetClassAllocationDTO(
                    entry.getKey(),
                    percentage,
                    value
                );
            })
            .toList();
    }

    // Calculates gain/loss percentage: (gain/loss dollars / cost basis) * 100, returns 0 if cost basis is zero
    private BigDecimal calculateGainLossPercent(BigDecimal gainLossDollars, BigDecimal totalCostBasis) {
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            return gainLossDollars.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
}