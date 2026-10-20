package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.services.AccountService;
import com.agentsbackend.services.MissionAuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.agentsbackend.services.TransactionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Account endpoints.
 * Handles all account-related API operations including CRUD, deposits, withdrawals,
 * portfolio summary, and performance metrics.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management, balances, and transactions")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final MissionAuthorizationService authorizationService;
    private final TransactionsService transactionsService;

    public AccountController(AccountService accountService,
                             MissionAuthorizationService authorizationService,
                             TransactionsService transactionsService) {
        this.accountService = accountService;
        this.authorizationService = authorizationService;
        this.transactionsService = transactionsService;
    }

    /**
     * List all accounts for the authenticated client.
     * GET /api/accounts
     *
     * @return ResponseEntity with list of AccountListItem (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "List accounts", description = "Returns all accounts belonging to the authenticated client.")
    @ApiResponse(responseCode = "200", description = "Accounts returned")
    public ResponseEntity<List<AccountsResponse.AccountListItem>> listAccounts(@AuthenticationPrincipal Jwt jwt) {
        List<AccountsResponse.AccountListItem> accounts = accountService.listAccounts(clientId(jwt));
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
    @Operation(summary = "Create an account", description = "Creates a new trading account for the authenticated client.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created"),
        @ApiResponse(responseCode = "400", description = "Invalid account request")
    })
    public ResponseEntity<AccountsResponse.Account> createAccount(@AuthenticationPrincipal Jwt jwt,
                                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                       description = "Account creation details",
                                                                       required = true,
                                                                       content = @Content(schema = @Schema(implementation = AccountsRequest.CreateAccount.class))
                                                                   )
                                                                   @Valid @RequestBody AccountsRequest.CreateAccount request) {
        authorizationService.requireOwnClient(clientId(jwt), request.getClientId());
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
    @Operation(summary = "Get account details", description = "Returns details for an account owned by the authenticated client.")
    @ApiResponse(responseCode = "200", description = "Account details returned")
    public ResponseEntity<AccountsResponse.AccountDetail> getAccountDetails(@AuthenticationPrincipal Jwt jwt,
                                                                             @Parameter(description = "Account identifier", required = true)
                                                                             @PathVariable("accountId") UUID accountId) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
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
    @Operation(summary = "Get account summary", description = "Returns cash, holdings, and total portfolio value for an account.")
    @ApiResponse(responseCode = "200", description = "Account summary returned")
    public ResponseEntity<AccountsResponse.Summary> getAccountSummary(@AuthenticationPrincipal Jwt jwt,
                                                                       @Parameter(description = "Account identifier", required = true)
                                                                       @PathVariable("accountId") UUID accountId) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
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
        @Operation(summary = "Get account performance", description = "Returns performance metrics for an account and period.")
        @ApiResponse(responseCode = "200", description = "Performance metrics returned")
    public ResponseEntity<AccountsResponse.Performance> getAccountPerformance(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @Parameter(description = "Performance period: 1D, 1W, 1M, 3M, 6M, 1Y, or ALL", example = "1Y")
            @RequestParam(value = "period", defaultValue = "1Y") String period) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
        AccountsResponse.Performance performance = accountService.getAccountPerformance(accountId, period);
        return ResponseEntity.ok(performance);
    }

    /**
     * Retrieve recent transactions (deposits and withdrawals) for an account.
     * GET /api/accounts/{accountId}/transactions?limit=10
     *
     * Query Parameters:
     * - limit: Maximum number of recent transactions to return (default: 10)
     *
     * @param accountId UUID of the account
     * @param limit Maximum number of transactions to return (default: 10)
     * @return ResponseEntity with list of TransactionItem ordered by most recent first (HTTP 200)
     */
    @GetMapping("/{accountId}/transactions")
        @Operation(summary = "Get recent transactions", description = "Returns recent deposits and withdrawals for an account.")
        @ApiResponse(responseCode = "200", description = "Transactions returned")
    public ResponseEntity<List<AccountsResponse.TransactionItem>> getRecentTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @Parameter(description = "Maximum number of transactions", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
        List<AccountsResponse.TransactionItem> transactions = transactionsService.getRecentTransactions(accountId, limit);
        return ResponseEntity.ok(transactions);
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
    @Operation(summary = "Update an account", description = "Updates an account name or status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account updated"),
        @ApiResponse(responseCode = "400", description = "Invalid account update")
    })
    public ResponseEntity<AccountsResponse.Account> updateAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Account update details",
                required = true,
                content = @Content(schema = @Schema(implementation = AccountsRequest.UpdateAccount.class))
            )
            @Valid @RequestBody AccountsRequest.UpdateAccount request) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
        AccountsResponse.Account account = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(account);
    }

    /**
     * Deposit cash into an account.
     * PATCH /api/accounts/{accountId}/deposit
     *
     * @param accountId UUID of the account
     * @param request AccountsRequest.Deposit containing deposit amount
     * @return ResponseEntity with AccountsResponse.Transaction showing new balance (HTTP 200)
     */
    @PatchMapping("/{accountId}/deposit")
    @Operation(summary = "Deposit cash", description = "Deposits cash into an account.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deposit completed"),
        @ApiResponse(responseCode = "400", description = "Invalid deposit amount")
    })
    public ResponseEntity<AccountsResponse.Transaction> depositCash(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Deposit amount",
                required = true,
                content = @Content(schema = @Schema(implementation = AccountsRequest.Deposit.class))
            )
            @Valid @RequestBody AccountsRequest.Deposit request) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
        AccountsResponse.Transaction response = accountService.depositCash(accountId, request.getAmount());
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw cash from an account.
     * PATCH /api/accounts/{accountId}/withdraw
     *
     * @param accountId UUID of the account
     * @param request AccountsRequest.Withdrawal containing withdrawal amount
     * @return ResponseEntity with AccountsResponse.Transaction showing new balance (HTTP 200)
     * @throws IllegalArgumentException if insufficient funds or account is closed
     */
    @PatchMapping("/{accountId}/withdraw")
    @Operation(summary = "Withdraw cash", description = "Withdraws cash from an account when sufficient funds are available.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Withdrawal completed"),
        @ApiResponse(responseCode = "400", description = "Invalid withdrawal or insufficient funds")
    })
    public ResponseEntity<AccountsResponse.Transaction> withdrawCash(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("accountId") UUID accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Withdrawal amount",
                required = true,
                content = @Content(schema = @Schema(implementation = AccountsRequest.Withdrawal.class))
            )
            @Valid @RequestBody AccountsRequest.Withdrawal request) {
        authorizationService.requireOwnAccount(clientId(jwt), accountId);
        AccountsResponse.Transaction response = accountService.withdrawCash(accountId, request.getAmount());
        return ResponseEntity.ok(response);
    }

    private UUID clientId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
