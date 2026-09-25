package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.repos.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountServiceImpl business logic.
 * Tests service behavior with mocked repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private Client testClient;
    private UUID testAccountId;
    private UUID testClientId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testClientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setClientId(testClientId);

        testAccount = new Account();
        testAccount.setAccountId(testAccountId);
        testAccount.setClient(testClient);
        testAccount.setName("Test Account");
        testAccount.setCashBalance(new BigDecimal("50000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setOpenDate(LocalDateTime.now());
    }

    // ===== LIST ACCOUNTS TESTS =====
    @Test
    @DisplayName("List all accounts successfully")
    void testListAccounts() {
        when(accountRepository.findAll()).thenReturn(java.util.List.of(testAccount));
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("30000.00"));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(BigDecimal.ZERO);

        var result = accountService.listAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccountId, result.get(0).getAccountId());
    }

    // ===== CREATE ACCOUNT TESTS =====
    @Test
    @DisplayName("Create account successfully")
    void testCreateAccount_Success() {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(testClientId);
        request.setName("New Account");
        request.setInitialCashBalance(new BigDecimal("10000.00"));

        when(accountRepository.clientExists(testClientId)).thenReturn(true);
        doNothing().when(accountRepository).createAccount(any(Account.class));

        var result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals("New Account", result.getName());
        assertEquals(new BigDecimal("10000.00"), result.getCashBalance());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(accountRepository, times(1)).clientExists(testClientId);
        verify(accountRepository, times(1)).createAccount(any(Account.class));
    }

    @Test
    @DisplayName("Create account with client not found")
    void testCreateAccount_ClientNotFound() {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(testClientId);
        request.setName("New Account");
        request.setInitialCashBalance(new BigDecimal("10000.00"));

        when(accountRepository.clientExists(testClientId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(request));
    }

    @Test
    @DisplayName("Create account with zero balance")
    void testCreateAccount_ZeroBalance() {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(testClientId);
        request.setName("Zero Balance Account");
        request.setInitialCashBalance(BigDecimal.ZERO);

        when(accountRepository.clientExists(testClientId)).thenReturn(true);
        doNothing().when(accountRepository).createAccount(any(Account.class));

        var result = accountService.createAccount(request);

        assertEquals(BigDecimal.ZERO, result.getCashBalance());
    }

    @Test
    @DisplayName("Create account with large balance")
    void testCreateAccount_LargeBalance() {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(testClientId);
        request.setName("Large Balance Account");
        request.setInitialCashBalance(new BigDecimal("1000000.00"));

        when(accountRepository.clientExists(testClientId)).thenReturn(true);
        doNothing().when(accountRepository).createAccount(any(Account.class));

        var result = accountService.createAccount(request);

        assertEquals(new BigDecimal("1000000.00"), result.getCashBalance());
    }

    // ===== GET ACCOUNT DETAILS TESTS =====
    @Test
    @DisplayName("Get account details successfully")
    void testGetAccountDetails_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getHoldingCount(testAccountId)).thenReturn(5);
        when(accountRepository.getOrderCount(testAccountId)).thenReturn(3);
        when(accountRepository.getTransactionCount(testAccountId)).thenReturn(10);

        var result = accountService.getAccountDetails(testAccountId);

        assertNotNull(result);
        assertEquals(testAccountId, result.getAccountId());
        assertEquals(5, result.getHoldingCount());
        assertEquals(3, result.getOrderCount());
        assertEquals(10, result.getTransactionCount());
    }

    @Test
    @DisplayName("Get account details not found")
    void testGetAccountDetails_NotFound() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountDetails(testAccountId));
    }

    @Test
    @DisplayName("Get account details with null counts")
    void testGetAccountDetails_NullCounts() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getHoldingCount(testAccountId)).thenReturn(null);
        when(accountRepository.getOrderCount(testAccountId)).thenReturn(null);
        when(accountRepository.getTransactionCount(testAccountId)).thenReturn(null);

        var result = accountService.getAccountDetails(testAccountId);

        assertNotNull(result);
        // Verify that null values are preserved
        assertTrue(result.getHoldingCount() == null || result.getHoldingCount() == 0);
    }

    // ===== DEPOSIT CASH TESTS =====
    @Test
    @DisplayName("Deposit cash successfully")
    void testDepositCash_Success() {
        BigDecimal amount = new BigDecimal("5000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).updateCashBalance(any(UUID.class), any(BigDecimal.class));
        doNothing().when(accountRepository).recordTransaction(any(UUID.class), any(BigDecimal.class), anyString());

        var response = accountService.depositCash(testAccountId, amount);

        assertNotNull(response);
        assertEquals("DEPOSIT", response.getTransactionType());
        assertTrue(response.getNewCashBalance().compareTo(new BigDecimal("50000.00")) > 0);
    }

    @Test
    @DisplayName("Deposit cash with negative amount")
    void testDepositCash_NegativeAmount() {
        BigDecimal amount = new BigDecimal("-5000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.depositCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Deposit cash with zero amount")
    void testDepositCash_ZeroAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.depositCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Deposit cash to inactive account")
    void testDepositCash_InactiveAccount() {
        testAccount.setStatus(AccountStatus.CLOSED);
        BigDecimal amount = new BigDecimal("5000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.depositCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Deposit large amount")
    void testDepositCash_LargeAmount() {
        BigDecimal amount = new BigDecimal("500000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).updateCashBalance(any(UUID.class), any(BigDecimal.class));
        doNothing().when(accountRepository).recordTransaction(any(UUID.class), any(BigDecimal.class), anyString());

        var response = accountService.depositCash(testAccountId, amount);

        assertNotNull(response);
        assertEquals("DEPOSIT", response.getTransactionType());
    }

    // ===== WITHDRAW CASH TESTS =====
    @Test
    @DisplayName("Withdraw cash successfully")
    void testWithdrawCash_Success() {
        BigDecimal amount = new BigDecimal("10000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(BigDecimal.ZERO);
        doNothing().when(accountRepository).updateCashBalance(any(UUID.class), any(BigDecimal.class));
        doNothing().when(accountRepository).recordTransaction(any(UUID.class), any(BigDecimal.class), anyString());

        var response = accountService.withdrawCash(testAccountId, amount);

        assertNotNull(response);
        assertEquals("WITHDRAWAL", response.getTransactionType());
    }

    @Test
    @DisplayName("Withdraw cash insufficient funds")
    void testWithdrawCash_InsufficientFunds() {
        BigDecimal amount = new BigDecimal("60000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Withdraw cash with reserved funds")
    void testWithdrawCash_WithReservedFunds() {
        // Cash: 50000, Requested: 40000, Reserved: 15000
        // Available: 50000 - 15000 = 35000, so 40000 > 35000 should fail
        BigDecimal amount = new BigDecimal("40000.00");
        BigDecimal reserved = new BigDecimal("15000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(reserved);

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Withdraw cash exceeds available")
    void testWithdrawCash_ExceedsAvailable() {
        BigDecimal amount = new BigDecimal("45000.00");
        BigDecimal reserved = new BigDecimal("10000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(reserved);

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Withdraw cash negative amount")
    void testWithdrawCash_NegativeAmount() {
        BigDecimal amount = new BigDecimal("-5000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Withdraw cash zero amount")
    void testWithdrawCash_ZeroAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    @Test
    @DisplayName("Withdraw cash from inactive account")
    void testWithdrawCash_InactiveAccount() {
        testAccount.setStatus(AccountStatus.CLOSED);
        BigDecimal amount = new BigDecimal("5000.00");
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawCash(testAccountId, amount));
    }

    // ===== GET ACCOUNT SUMMARY TESTS =====
    @Test
    @DisplayName("Get account summary successfully")
    void testGetAccountSummary_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("30000.00"));
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(new BigDecimal("5000.00"));

        var result = accountService.getAccountSummary(testAccountId);

        assertNotNull(result);
        assertEquals(new BigDecimal("50000.00"), result.getCashBalance());
        assertEquals(new BigDecimal("45000.00"), result.getAvailableBalance());
        assertEquals(new BigDecimal("80000.00"), result.getTotalValue());
    }

    @Test
    @DisplayName("Get account summary with null portfolio value")
    void testGetAccountSummary_NullPortfolioValue() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(null);
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(BigDecimal.ZERO);

        var result = accountService.getAccountSummary(testAccountId);

        assertNotNull(result);
        assertEquals(new BigDecimal("50000.00"), result.getTotalValue());
    }

    @Test
    @DisplayName("Get account summary with zero portfolio value")
    void testGetAccountSummary_ZeroPortfolioValue() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(BigDecimal.ZERO);
        when(accountRepository.getReservedFundsForOpenOrders(testAccountId)).thenReturn(BigDecimal.ZERO);

        var result = accountService.getAccountSummary(testAccountId);

        assertEquals(new BigDecimal("50000.00"), result.getTotalValue());
    }

    @Test
    @DisplayName("Get account summary not found")
    void testGetAccountSummary_NotFound() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountSummary(testAccountId));
    }

    // ===== GET PERFORMANCE TESTS =====
    @Test
    @DisplayName("Get performance 1 day")
    void testGetPerformance_1D() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("29000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("31000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(BigDecimal.ZERO);

        var result = accountService.getAccountPerformance(testAccountId, "1D");

        assertNotNull(result);
        assertEquals("1D", result.getPeriod());
    }

    @Test
    @DisplayName("Get performance 1 week")
    void testGetPerformance_1W() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("28500.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("31500.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(BigDecimal.ZERO);

        var result = accountService.getAccountPerformance(testAccountId, "1W");

        assertNotNull(result);
        assertEquals("1W", result.getPeriod());
    }

    @Test
    @DisplayName("Get performance 1 month")
    void testGetPerformance_1M() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("32000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(BigDecimal.ZERO);

        var result = accountService.getAccountPerformance(testAccountId, "1M");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Get performance 3 months")
    void testGetPerformance_3M() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("27000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("33000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(new BigDecimal("1000.00"));

        var result = accountService.getAccountPerformance(testAccountId, "3M");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Get performance 6 months")
    void testGetPerformance_6M() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("26000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("34000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(new BigDecimal("2000.00"));

        var result = accountService.getAccountPerformance(testAccountId, "6M");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Get performance 1 year")
    void testGetPerformance_1Y() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("25000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("35000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(new BigDecimal("3000.00"));

        var result = accountService.getAccountPerformance(testAccountId, "1Y");

        assertNotNull(result);
        assertEquals("1Y", result.getPeriod());
    }

    @Test
    @DisplayName("Get performance all time")
    void testGetPerformance_ALL() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("20000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("40000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("28000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(new BigDecimal("5000.00"));

        var result = accountService.getAccountPerformance(testAccountId, "ALL");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Get performance invalid period")
    void testGetPerformance_InvalidPeriod() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountPerformance(testAccountId, "INVALID"));
    }

    @Test
    @DisplayName("Get performance not found")
    void testGetPerformance_NotFound() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountPerformance(testAccountId, "1Y"));
    }

    @Test
    @DisplayName("Get performance negative return")
    void testGetPerformance_NegativeReturn() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.getPortfolioValueAtDate(eq(testAccountId), any())).thenReturn(new BigDecimal("100000.00"));
        when(accountRepository.getPortfolioValue(testAccountId)).thenReturn(new BigDecimal("30000.00"));
        when(accountRepository.getHoldingsCostBasis(testAccountId)).thenReturn(new BigDecimal("45000.00"));
        when(accountRepository.getRealizedGainLoss(eq(testAccountId), any())).thenReturn(new BigDecimal("-5000.00"));

        var perf = accountService.getAccountPerformance(testAccountId, "1Y");

        assertNotNull(perf);
        assertTrue(perf.getTotalReturn().compareTo(BigDecimal.ZERO) < 0);
    }

    // ===== UPDATE ACCOUNT TESTS =====
    @Test
    @DisplayName("Update account successfully")
    void testUpdateAccount_Success() {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        request.setName("Updated Name");
        request.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).updateAccount(any(Account.class));

        var result = accountService.updateAccount(testAccountId, request);

        assertNotNull(result);
        verify(accountRepository, times(1)).updateAccount(any(Account.class));
    }

    @Test
    @DisplayName("Update account only name")
    void testUpdateAccount_OnlyName() {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        request.setName("New Name Only");

        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).updateAccount(any(Account.class));

        var result = accountService.updateAccount(testAccountId, request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Update account only status")
    void testUpdateAccount_OnlyStatus() {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        request.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).updateAccount(any(Account.class));

        var result = accountService.updateAccount(testAccountId, request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Update account not found")
    void testUpdateAccount_NotFound() {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.updateAccount(testAccountId, request));
    }
}
