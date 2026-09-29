package com.agentsbackend.services;

import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.exceptions.AccountNotFoundException;
import com.agentsbackend.exceptions.HoldingNotFoundException;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.InstrumentPriceRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService{
    private final AccountRepository accountRepository;
    
    private final HoldingService holdingService;

    public PortfolioServiceImpl(AccountRepository accountRepository,
                             HoldingService holdingService) {
        this.accountRepository = accountRepository;
        this.holdingService = holdingService;
    }

    @Override
    public GetAccountPortfolioResponseDTO getPortfolioByAccountId(UUID accountId) {

        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

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

    

    private BigDecimal calculateGainLossPercent(BigDecimal gainLossDollars, BigDecimal totalCostBasis) {
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            return gainLossDollars.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
}
