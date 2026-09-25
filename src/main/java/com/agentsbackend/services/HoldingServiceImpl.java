package com.agentsbackend.services;

import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.InstrumentRepository;
import com.agentsbackend.DTO.requests.CreateHoldingRequestDTO;
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

    public HoldingServiceImpl(HoldingRepository holdingRepository,
                             AccountRepository accountRepository,
                             InstrumentRepository instrumentRepository) {
        this.holdingRepository = holdingRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
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
    public Holding getHolding(UUID holdingId){
        return holdingRepository.findById(holdingId)
            .orElseThrow(() -> new HoldingNotFoundException(holdingId));
    }
}

