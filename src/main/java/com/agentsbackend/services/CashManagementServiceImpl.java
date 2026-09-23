package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.CashMovementRequest;
import com.agentsbackend.DTO.response.AccountBalanceResponse;
import com.agentsbackend.enums.TransactionType;
import com.agentsbackend.repos.CashManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CashManagementServiceImpl implements CashManagementService {

    private final CashManagementRepository cashManagementRepository;

    public CashManagementServiceImpl(CashManagementRepository cashManagementRepository) {
        this.cashManagementRepository = cashManagementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getBalance(UUID accountId) {
        BigDecimal balance = cashManagementRepository.getCashBalance(accountId);
        if (balance == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        return new AccountBalanceResponse(accountId, balance);
    }

    @Override
    public AccountBalanceResponse deposit(UUID accountId, CashMovementRequest request) {
        validateAmount(request.amount());
        BigDecimal delta = request.amount();
        int updated = cashManagementRepository.updateCashBalance(accountId, delta);
        if (updated == 0) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        cashManagementRepository.insertTransaction(UUID.randomUUID(), accountId, TransactionType.DEPOSIT, request.amount(), LocalDateTime.now());
        return getBalance(accountId);
    }

    @Override
    public AccountBalanceResponse withdraw(UUID accountId, CashMovementRequest request) {
        validateAmount(request.amount());
        BigDecimal currentBalance = cashManagementRepository.getCashBalance(accountId);
        if (currentBalance == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        if (currentBalance.compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }

        int updated = cashManagementRepository.updateCashBalance(accountId, request.amount().negate());
        if (updated == 0) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        cashManagementRepository.insertTransaction(UUID.randomUUID(), accountId, TransactionType.WITHDRAWAL, request.amount(), LocalDateTime.now());
        return getBalance(accountId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
