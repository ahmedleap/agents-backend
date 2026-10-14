package com.agentsbackend.services;

import com.agentsbackend.DTO.response.AccountsResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Transaction operations.
 * Defines business logic contracts for transaction management including
 * retrieval of recent cash transactions (deposits and withdrawals).
 */
public interface TransactionsService {

    /**
     * Retrieve recent transactions for a specific account.
     * Returns the N most recent transactions (deposits and withdrawals) ordered by creation date (newest first).
     *
     * @param accountId UUID of the account
     * @param limit Maximum number of transactions to return (default: 10)
     * @return List of AccountsResponse.TransactionItem representing recent transactions
     * @throws IllegalArgumentException if account not found
     */
    List<AccountsResponse.TransactionItem> getRecentTransactions(UUID accountId, int limit);

    /**
     * Retrieve all transactions for a specific account.
     * Returns all transactions (deposits and withdrawals) ordered by creation date (newest first).
     *
     * @param accountId UUID of the account
     * @return List of AccountsResponse.TransactionItem representing all transactions
     * @throws IllegalArgumentException if account not found
     */
    List<AccountsResponse.TransactionItem> getAllTransactions(UUID accountId);
}
