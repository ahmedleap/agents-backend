package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.InstrumentPriceRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService{
    private final HoldingRepository holdingRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;

    private final HoldingService holdingService;

    public PortfolioServiceImpl(HoldingRepository holdingRepository,
                             AccountRepository accountRepository,
                             InstrumentRepository instrumentRepository,
                             InstrumentPriceRepository instrumentPriceRepository,
                             HoldingService holdingService) {
        this.holdingRepository = holdingRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.instrumentPriceRepository = instrumentPriceRepository;
        this.holdingService = holdingService;
    }

    @Override
    public List<GetHoldingResponseDTO> getPortfolioByAccountId(UUID accountId) {
        return holdingService.getHoldingsDTOByAccountId(accountId);
    }
}
