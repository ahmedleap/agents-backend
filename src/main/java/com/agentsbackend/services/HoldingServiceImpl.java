package com.agentsbackend.services;

import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import com.agentsbackend.repos.InstrumentPriceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.exceptions.AccountNotFoundException;
import com.agentsbackend.exceptions.HoldingNotFoundException;
import com.agentsbackend.exceptions.InstrumentNotFoundException;
import com.agentsbackend.services.HoldingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;

    public HoldingServiceImpl(HoldingRepository holdingRepository,
                             AccountRepository accountRepository,
                             InstrumentRepository instrumentRepository,
                             InstrumentPriceRepository instrumentPriceRepository) {
        this.holdingRepository = holdingRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.instrumentPriceRepository = instrumentPriceRepository;
    }
    
    // Creates a new admin with auto-generated UUID and current timestamp if not provided
    @Override
    public Holding createHoldingFromDTO(CreateHoldingRequestDTO request) {
        Account account = accountRepository.findById(UUID.fromString(request.getAccountId()))
            .orElseThrow(() -> new AccountNotFoundException(UUID.fromString(request.getAccountId())));

        Instrument instrument = instrumentRepository.findById(UUID.fromString(request.getInstrumentId()))
            .orElseThrow(() -> new InstrumentNotFoundException(UUID.fromString(request.getInstrumentId())));

        Holding holding = new Holding();
        holding.setAccount(account);
        holding.setInstrument(instrument);
        holding.setQuantity(request.getQuantity());
        holding.setAverageCostBasis(request.getAverageCostBasis());

        return createHolding(holding);
    }

    @Override
    public Holding createHolding(Holding holding){
        if (holding.getHoldingId() == null){
            holding.setHoldingId(UUID.randomUUID());
        }
        holdingRepository.createHolding(holding);
        return holding;
    }

    @Override
    public java.util.List<Holding> getHoldingsByAccountId(UUID accountId){
        return holdingRepository.findByAccountId(accountId);
    }

    @Override
    public BigDecimal getCurrentPrice(UUID instrumentId){
        return instrumentPriceRepository.getLatestPrice(instrumentId)
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
}

