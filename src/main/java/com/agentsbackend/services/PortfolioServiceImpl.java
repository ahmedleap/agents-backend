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
import com.agentsbackend.repos.InstrumentPriceRepository;
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
    private final InstrumentPriceRepository instrumentPriceRepository;
    private final ClientRepository clientRepository;

    public PortfolioServiceImpl(AccountRepository accountRepository,
                             HoldingService holdingService,
                             InstrumentPriceRepository instrumentPriceRepository,
                             ClientRepository clientRepository) {
        this.accountRepository = accountRepository;
        this.holdingService = holdingService;
        this.instrumentPriceRepository = instrumentPriceRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public GetAccountPortfolioResponseDTO getPortfolioByAccountId(UUID clientId, UUID accountId) {

        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Verify the account belongs to the client
        if (!account.getClient().getClientId().equals(clientId)) {
            throw new UnauthorizedAccountAccessException(clientId, accountId);
        }

        List<GetHoldingResponseDTO> accountHoldings;
        accountHoldings = holdingService.getHoldingsDTOByAccountId(accountId);

        BigDecimal totalValue = accountHoldings.stream()
            .map(GetHoldingResponseDTO::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = accountHoldings.stream()
            .map(h -> h.getQuantity().multiply(h.getAverageCostBasis()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gainLossDollars = totalValue.subtract(totalCost);
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
        // Verify client exists
        clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));
        
        List<Account> accounts = accountRepository.findByClientId(clientId);

        List<GetAccountPortfolioResponseDTO> portfolios = accounts.stream()
            .map(account -> getPortfolioByAccountId(clientId, account.getAccountId()))
            .toList();

        BigDecimal totalPortfolioValue = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalPortfolioValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostBasis = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalCostBasis)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalGainLossDollars = portfolios.stream()
            .map(GetAccountPortfolioResponseDTO::getTotalGainLossDollars)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
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
        // Verify client exists
        clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));
        
        // Get all accounts for the client
        List<Account> accounts = accountRepository.findByClientId(clientId);
        
        // Collect all holdings across all accounts
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
        // If no holdings, return empty allocation
        if (holdings.isEmpty()) {
            return new GetAllocationResponseDTO(List.of(), List.of());
        }

        // Calculate total value
        BigDecimal totalValue = calculateTotalValue(holdings);

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
        return holdings.stream()
            .map(holding -> {
                BigDecimal price = instrumentPriceRepository.getLatestPrice(holding.getInstrument().getInstrumentId())
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
        Map<String, BigDecimal> industryValues = holdings.stream()
            .collect(Collectors.groupingBy(
                holding -> holding.getInstrument().getIndustry() != null 
                    ? holding.getInstrument().getIndustry() 
                    : "Unknown",
                Collectors.reducing(
                    BigDecimal.ZERO,
                    holding -> {
                        BigDecimal price = instrumentPriceRepository.getLatestPrice(holding.getInstrument().getInstrumentId())
                            .orElse(BigDecimal.ZERO);
                        return holding.getQuantity().multiply(price);
                    },
                    BigDecimal::add
                )
            ));

        return industryValues.entrySet().stream()
            .map(entry -> {
                BigDecimal value = entry.getValue();
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
        Map<String, BigDecimal> assetClassValues = holdings.stream()
            .collect(Collectors.groupingBy(
                holding -> holding.getInstrument().getAssetClass().toString(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    holding -> {
                        BigDecimal price = instrumentPriceRepository.getLatestPrice(holding.getInstrument().getInstrumentId())
                            .orElse(BigDecimal.ZERO);
                        return holding.getQuantity().multiply(price);
                    },
                    BigDecimal::add
                )
            ));

        return assetClassValues.entrySet().stream()
            .map(entry -> {
                BigDecimal value = entry.getValue();
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

    private BigDecimal calculateGainLossPercent(BigDecimal gainLossDollars, BigDecimal totalCostBasis) {
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            return gainLossDollars.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
}
