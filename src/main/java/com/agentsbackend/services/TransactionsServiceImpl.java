package com.agentsbackend.services;

import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Transaction;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.TransactionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionsService.
 * Handles business logic for transaction operations including retrieval
 * of recent cash transactions (deposits and withdrawals).
 */
@Service
@Transactional(readOnly = true)
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;

    public TransactionsServiceImpl(TransactionsRepository transactionsRepository,
                                   AccountRepository accountRepository) {
        this.transactionsRepository = transactionsRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieve recent transactions for a specific account.
     * Returns the N most recent transactions (deposits and withdrawals) ordered by creation date (newest first).
     *
     * @param accountId UUID of the account
     * @param limit Maximum number of transactions to return (default: 10)
     * @return List of AccountsResponse.TransactionItem representing recent transactions
     * @throws IllegalArgumentException if account not found
     */
    @Override
    public List<AccountsResponse.TransactionItem> getRecentTransactions(UUID accountId, int limit) {
        // Verify account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Retrieve recent transactions from repository
        List<Transaction> transactions = transactionsRepository.findRecentTransactions(accountId, limit);

        // Convert to response DTOs
        return transactions.stream()
                .map(this::convertToTransactionItem)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all transactions for a specific account.
     * Returns all transactions (deposits and withdrawals) ordered by creation date (newest first).
     *
     * @param accountId UUID of the account
     * @return List of AccountsResponse.TransactionItem representing all transactions
     * @throws IllegalArgumentException if account not found
     */
    @Override
    public List<AccountsResponse.TransactionItem> getAllTransactions(UUID accountId) {
        // Verify account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Retrieve all transactions from repository
        List<Transaction> transactions = transactionsRepository.findByAccountId(accountId);

        // Convert to response DTOs
        return transactions.stream()
                .map(this::convertToTransactionItem)
                .collect(Collectors.toList());
    }

    /**
     * Convert Transaction entity to TransactionItem response DTO.
     *
     * @param transaction Transaction entity to convert
     * @return TransactionItem response DTO
     */
    private AccountsResponse.TransactionItem convertToTransactionItem(Transaction transaction) {
        return new AccountsResponse.TransactionItem(
                transaction.getTransactionId(),
                transaction.getTxnType().toString(),
                transaction.getAmount(),
                transaction.getCreatedAt()
        );
    }
}
