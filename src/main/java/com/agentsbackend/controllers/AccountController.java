package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Account endpoints.
 * Handles all account-related API operations including CRUD, deposits, withdrawals,
 * portfolio summary, and performance metrics.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * List all accounts for the authenticated client.
     * GET /api/accounts
     *
     * @return ResponseEntity with list of AccountListItem (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<AccountsResponse.AccountListItem>> listAccounts() {
        List<AccountsResponse.AccountListItem> accounts = accountService.listAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Create a new account for a client.
     * POST /api/accounts
     *
     * @param request AccountsRequest.CreateAccount containing client ID, name, and initial cash balance
     * @return ResponseEntity with created AccountsResponse.Account (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<AccountsResponse.Account> createAccount(@Valid @RequestBody AccountsRequest.CreateAccount request) {
        AccountsResponse.Account account = accountService.createAccount(request);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    /**
     * Retrieve account details by account ID.
     * GET /api/accounts/{accountId}
     *
     * @param accountId UUID of the account to retrieve
     * @return ResponseEntity with AccountsResponse.AccountDetail (HTTP 200)
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountsResponse.AccountDetail> getAccountDetails(@PathVariable UUID accountId) {
        AccountsResponse.AccountDetail account = accountService.getAccountDetails(accountId);
        return ResponseEntity.ok(account);
    }

    /**
     * Retrieve account portfolio summary with cash, holdings, and total value.
     * GET /api/accounts/{accountId}/summary
     *
     * @param accountId UUID of the account
     * @return ResponseEntity with AccountsResponse.Summary showing portfolio valuation (HTTP 200)
     */
    @GetMapping("/{accountId}/summary")
    public ResponseEntity<AccountsResponse.Summary> getAccountSummary(@PathVariable UUID accountId) {
        AccountsResponse.Summary summary = accountService.getAccountSummary(accountId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieve account performance metrics for a specified period.
     * GET /api/accounts/{accountId}/performance?period=1Y
     *
     * Query Parameters:
     * - period: Performance period (1D, 1W, 1M, 3M, 6M, 1Y, ALL) - defaults to 1Y
     *
     * @param accountId UUID of the account
     * @param period Performance period string (default: "1Y")
     * @return ResponseEntity with AccountsResponse.Performance (HTTP 200)
     */
    @GetMapping("/{accountId}/performance")
    public ResponseEntity<AccountsResponse.Performance> getAccountPerformance(
            @PathVariable UUID accountId,
            @RequestParam(value = "period", defaultValue = "1Y") String period) {
        AccountsResponse.Performance performance = accountService.getAccountPerformance(accountId, period);
        return ResponseEntity.ok(performance);
    }

    /**
     * Update account details (name and/or status).
     * PUT /api/accounts/{accountId}
     *
     * @param accountId UUID of the account to update
     * @param request AccountsRequest.UpdateAccount containing updated fields
     * @return ResponseEntity with updated AccountsResponse.Account (HTTP 200)
     */
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountsResponse.Account> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountsRequest.UpdateAccount request) {
        AccountsResponse.Account account = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(account);
    }

    /**
     * Deposit cash into an account.
     * POST /api/accounts/{accountId}/deposit
     *
     * @param accountId UUID of the account
     * @param request AccountsRequest.Deposit containing deposit amount
     * @return ResponseEntity with AccountsResponse.Transaction showing new balance (HTTP 200)
     */
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountsResponse.Transaction> depositCash(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountsRequest.Deposit request) {
        AccountsResponse.Transaction response = accountService.depositCash(accountId, request.getAmount());
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw cash from an account.
     * POST /api/accounts/{accountId}/withdraw
     *
     * @param accountId UUID of the account
     * @param request AccountsRequest.Withdrawal containing withdrawal amount
     * @return ResponseEntity with AccountsResponse.Transaction showing new balance (HTTP 200)
     * @throws IllegalArgumentException if insufficient funds or account is closed
     */
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountsResponse.Transaction> withdrawCash(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountsRequest.Withdrawal request) {
        AccountsResponse.Transaction response = accountService.withdrawCash(accountId, request.getAmount());
        return ResponseEntity.ok(response);
    }
}
