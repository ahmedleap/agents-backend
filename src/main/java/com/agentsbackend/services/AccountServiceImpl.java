package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.repos.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AccountService.
 * Handles business logic for account operations including validation,
 * cash transactions, and database coordination.
 */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * List all accounts for the authenticated client with summary information.
     * Includes cash balance, available balance, and total portfolio value.
     */
    @Override
    public List<AccountsResponse.AccountListItem> listAccounts() {
        // TODO: Update to use authenticated client ID from security context
        // For now, returning all accounts - in production this should filter by current client
        List<Account> accounts = accountRepository.findAll();
        
        return accounts.stream()
                .map(account -> {
                    BigDecimal availableBalance = calculateAvailableBalance(account.getAccountId());
                    BigDecimal holdingsValue = accountRepository.getPortfolioValue(account.getAccountId());
                    if (holdingsValue == null) {
                        holdingsValue = BigDecimal.ZERO;
                    }
                    BigDecimal totalValue = account.getCashBalance().add(holdingsValue);
                    
                    return new AccountsResponse.AccountListItem(
                            account.getAccountId(),
                            account.getName(),
                            account.getStatus(),
                            account.getCashBalance(),
                            availableBalance,
                            totalValue
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Create a new account with ACTIVE status and specified initial cash balance.
     * Validates that client exists before creating account.
     */
    @Override
    public AccountsResponse.Account createAccount(AccountsRequest.CreateAccount request) {
        // Validate that client exists
        if (!accountRepository.clientExists(request.getClientId())) {
            throw new IllegalArgumentException("Client not found: " + request.getClientId());
        }

        // Create account entity
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setClient(new Client()); // Client ID will be set via DB
        account.getClient().setClientId(request.getClientId());
        account.setName(request.getName());
        account.setCashBalance(request.getInitialCashBalance());
        account.setStatus(AccountStatus.ACTIVE);
        account.setOpenDate(LocalDateTime.now());

        // Persist account
        accountRepository.createAccount(account);

        // Return response DTO
        return entityToResponse(account);
    }

    /**
     * Retrieve detailed account information including related counts.
     * Throws exception if account not found.
     */
    @Override
    public AccountsResponse.AccountDetail getAccountDetails(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        Integer holdingCount = accountRepository.getHoldingCount(accountId);
        Integer orderCount = accountRepository.getOrderCount(accountId);
        Integer transactionCount = accountRepository.getTransactionCount(accountId);
        BigDecimal availableBalance = calculateAvailableBalance(accountId);

        return new AccountsResponse.AccountDetail(
                account.getAccountId(),
                account.getClient().getClientId(),
                account.getName(),
                account.getCashBalance(),
                availableBalance,
                account.getStatus(),
                account.getOpenDate(),
                holdingCount != null ? holdingCount : 0,
                orderCount != null ? orderCount : 0,
                transactionCount != null ? transactionCount : 0
        );
    }

    /**
     * Retrieve account portfolio summary with cash, holdings value, and total value.
     * Includes available balance for liquidity analysis.
     */
    @Override
    public AccountsResponse.Summary getAccountSummary(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal availableBalance = calculateAvailableBalance(accountId);
        BigDecimal holdingsValue = accountRepository.getPortfolioValue(accountId);
        if (holdingsValue == null) {
            holdingsValue = BigDecimal.ZERO;
        }
        BigDecimal totalValue = account.getCashBalance().add(holdingsValue);

        return new AccountsResponse.Summary(
                account.getCashBalance(),
                availableBalance,
                holdingsValue,
                totalValue,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Retrieve account performance metrics for a specified period.
     * Calculates returns, gains/losses, and performance statistics.
     */
    @Override
    public AccountsResponse.Performance getAccountPerformance(UUID accountId, String period) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Validate period
        if (!isValidPeriod(period)) {
            throw new IllegalArgumentException("Invalid period: " + period + ". Valid periods: 1D, 1W, 1M, 3M, 6M, 1Y, ALL");
        }

        // Get performance data from repository
        BigDecimal startingValue = accountRepository.getPortfolioValueAtDate(accountId, getPeriodStartDate(period));
        if (startingValue == null) {
            startingValue = account.getCashBalance(); // Default to current cash if no history
        }

        BigDecimal currentHoldingsValue = accountRepository.getPortfolioValue(accountId);
        if (currentHoldingsValue == null) {
            currentHoldingsValue = BigDecimal.ZERO;
        }
        BigDecimal endingValue = account.getCashBalance().add(currentHoldingsValue);

        // Calculate performance metrics
        BigDecimal totalReturn = endingValue.subtract(startingValue);
        BigDecimal returnPercentage = startingValue.signum() != 0
                ? totalReturn.multiply(new BigDecimal("100")).divide(startingValue, 4, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get realized and unrealized gains/losses
        BigDecimal realizedGainLoss = accountRepository.getRealizedGainLoss(accountId, getPeriodStartDate(period));
        if (realizedGainLoss == null) {
            realizedGainLoss = BigDecimal.ZERO;
        }

        BigDecimal unrealizedGainLoss = currentHoldingsValue.subtract(
                accountRepository.getHoldingsCostBasis(accountId)
        );
        if (unrealizedGainLoss == null) {
            unrealizedGainLoss = BigDecimal.ZERO;
        }

        return new AccountsResponse.Performance(
                period,
                startingValue,
                endingValue,
                totalReturn,
                returnPercentage,
                realizedGainLoss,
                unrealizedGainLoss,
                getPeriodStartDate(period).toString(),
                LocalDateTime.now().toString()
        );
    }

    /**
     * Retrieve all accounts for a client.
     * Returns empty list if client has no accounts.
     */
    @Override
    public List<AccountsResponse.Account> getAccountsByClient(UUID clientId) {
        // Validate client exists
        if (!accountRepository.clientExists(clientId)) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }

        List<Account> accounts = accountRepository.findByClientId(clientId);
        return accounts.stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update account name and/or status.
     * If status is being updated, validates valid transitions.
     */
    @Override
    public AccountsResponse.Account updateAccount(UUID accountId, AccountsRequest.UpdateAccount request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            account.setName(request.getName());
        }

        // Update status if provided
        if (request.getStatus() != null) {
            validateStatusTransition(account.getStatus(), request.getStatus());
            account.setStatus(request.getStatus());
        }

        // Persist updates
        accountRepository.updateAccount(account);

        return entityToResponse(account);
    }

    /**
     * Deposit cash into account.
     * Validates account is ACTIVE before processing.
     * Creates transaction record in DB.
     */
    @Override
    public AccountsResponse.Transaction depositCash(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Validate account is ACTIVE
        if (!account.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new IllegalArgumentException("Cannot deposit to account with status: " + account.getStatus());
        }

        // Validate amount
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // Calculate new balance
        BigDecimal newBalance = account.getCashBalance().add(amount);

        // Update cash balance in repository
        accountRepository.updateCashBalance(accountId, newBalance);

        // Record transaction in database
        accountRepository.recordTransaction(accountId, amount, "DEPOSIT");

        return new AccountsResponse.Transaction(
                "Deposit successful. Amount: " + amount,
                newBalance,
                "DEPOSIT"
        );
    }

    /**
     * Withdraw cash from account.
     * Validates account is ACTIVE and has sufficient funds.
     * Creates transaction record in DB.
     */
    @Override
    public AccountsResponse.Transaction withdrawCash(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Validate account is ACTIVE
        if (!account.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new IllegalArgumentException("Cannot withdraw from account with status: " + account.getStatus());
        }

        // Validate amount
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        // Validate sufficient funds (accounting for pending orders)
        BigDecimal availableBalance = calculateAvailableBalance(accountId);
        if (amount.compareTo(availableBalance) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Available: " + availableBalance + ", Requested: " + amount
            );
        }

        // Calculate new balance
        BigDecimal newBalance = account.getCashBalance().subtract(amount);

        // Update cash balance in repository
        accountRepository.updateCashBalance(accountId, newBalance);

        // Record transaction in database
        accountRepository.recordTransaction(accountId, amount, "WITHDRAWAL");

        return new AccountsResponse.Transaction(
                "Withdrawal successful. Amount: " + amount,
                newBalance,
                "WITHDRAWAL"
        );
    }

    /**
     * Convert Account entity to AccountsResponse.Account DTO.
     */
    private AccountsResponse.Account entityToResponse(Account account) {
        return new AccountsResponse.Account(
                account.getAccountId(),
                account.getClient().getClientId(),
                account.getName(),
                account.getCashBalance(),
                account.getStatus(),
                account.getOpenDate()
        );
    }

    /**
     * Validate status transitions.
     * ACTIVE -> RESTRICTED, SUSPENDED, CLOSED
     * RESTRICTED -> ACTIVE, SUSPENDED, CLOSED
     * SUSPENDED -> CLOSED
     * CLOSED -> no transitions allowed
     */
    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) {
        if (currentStatus.equals(newStatus)) {
            return; // Same status is allowed (no-op)
        }

        if (currentStatus.equals(AccountStatus.CLOSED)) {
            throw new IllegalArgumentException("Cannot transition from CLOSED status");
        }

        if (currentStatus.equals(AccountStatus.SUSPENDED) && !newStatus.equals(AccountStatus.CLOSED)) {
            throw new IllegalArgumentException("SUSPENDED account can only transition to CLOSED");
        }

        // All other transitions are valid
    }

    /**
     * Calculate available balance for an account.
     * Available balance = cash_balance - sum of reserved funds for open BUY orders
     * This ensures funds for pending orders cannot be withdrawn.
     */
    private BigDecimal calculateAvailableBalance(UUID accountId) {
        BigDecimal currentBalance = accountRepository.findById(accountId)
                .map(Account::getCashBalance)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal reservedFunds = accountRepository.getReservedFundsForOpenOrders(accountId);
        if (reservedFunds == null) {
            reservedFunds = BigDecimal.ZERO;
        }

        return currentBalance.subtract(reservedFunds);
    }

    /**
     * Validate performance period string.
     */
    private boolean isValidPeriod(String period) {
        return period.matches("^(1D|1W|1M|3M|6M|1Y|ALL)$");
    }

    /**
     * Get start date based on period string.
     */
    private LocalDateTime getPeriodStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "1D" -> now.minusDays(1);
            case "1W" -> now.minusWeeks(1);
            case "1M" -> now.minusMonths(1);
            case "3M" -> now.minusMonths(3);
            case "6M" -> now.minusMonths(6);
            case "1Y" -> now.minusYears(1);
            case "ALL" -> LocalDateTime.of(2000, 1, 1, 0, 0); // Beginning of time
            default -> now;
        };
    }

    /**
     * Find all accounts in system.
     * TODO: This should be scoped to current authenticated client in production.
     */
    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}
