package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Account operations.
 * Defines business logic contracts for account management including
 * CRUD operations, cash transactions, portfolio summary, and performance metrics.
 */
public interface AccountService {

    /**
     * List all accounts for the authenticated client with summary information.
     *
     * @return List of AccountsResponse.AccountListItem objects
     */
    List<AccountsResponse.AccountListItem> listAccounts();

    /**
     * Create a new account for a client.
     * Validates that client exists and initializes account with ACTIVE status.
     *
     * @param request AccountsRequest.CreateAccount with client ID, name, and initial cash balance
     * @return AccountsResponse.Account with created account details
     * @throws IllegalArgumentException if client not found
     */
    AccountsResponse.Account createAccount(AccountsRequest.CreateAccount request);

    /**
     * Retrieve detailed account information including related holdings and orders.
     *
     * @param accountId UUID of the account
     * @return AccountsResponse.AccountDetail with account details and related data counts
     * @throws IllegalArgumentException if account not found
     */
    AccountsResponse.AccountDetail getAccountDetails(UUID accountId);

    /**
     * Retrieve account portfolio summary with cash balance, holdings value, and total value.
     *
     * @param accountId UUID of the account
     * @return AccountsResponse.Summary with portfolio valuation details
     * @throws IllegalArgumentException if account not found
     */
    AccountsResponse.Summary getAccountSummary(UUID accountId);

    /**
     * Retrieve account performance metrics for a specified period.
     * Calculates returns, gains/losses, and performance statistics.
     *
     * @param accountId UUID of the account
     * @param period Period string (1D, 1W, 1M, 3M, 6M, 1Y, ALL)
     * @return AccountsResponse.Performance with performance metrics
     * @throws IllegalArgumentException if account not found or invalid period
     */
    AccountsResponse.Performance getAccountPerformance(UUID accountId, String period);

    /**
     * Retrieve all accounts belonging to a specific client.
     *
     * @param clientId UUID of the client
     * @return List of AccountsResponse.Account objects for the client
     * @throws IllegalArgumentException if client not found
     */
    List<AccountsResponse.Account> getAccountsByClient(UUID clientId);

    /**
     * Update account name and/or status.
     * Validates account status transitions.
     *
     * @param accountId UUID of the account to update
     * @param request AccountsRequest.UpdateAccount with new name and/or status
     * @return AccountsResponse.Account with updated details
     * @throws IllegalArgumentException if account not found or invalid status
     */
    AccountsResponse.Account updateAccount(UUID accountId, AccountsRequest.UpdateAccount request);

    /**
     * Deposit cash into an account.
     * Validates that account is in ACTIVE status.
     *
     * @param accountId UUID of the account
     * @param amount BigDecimal amount to deposit (must be positive)
     * @return AccountsResponse.Transaction with confirmation and new cash balance
     * @throws IllegalArgumentException if account not found or account is not ACTIVE
     */
    AccountsResponse.Transaction depositCash(UUID accountId, BigDecimal amount);

    /**
     * Withdraw cash from an account.
     * Validates that account is ACTIVE and has sufficient funds.
     *
     * @param accountId UUID of the account
     * @param amount BigDecimal amount to withdraw (must be positive)
     * @return AccountsResponse.Transaction with confirmation and new cash balance
     * @throws IllegalArgumentException if account not found, insufficient funds, or account not ACTIVE
     */
    AccountsResponse.Transaction withdrawCash(UUID accountId, BigDecimal amount);
}
