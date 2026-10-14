package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.services.AccountService;
import com.agentsbackend.services.TransactionsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AccountController REST endpoints.
 * Tests controller behavior with mocked service dependencies for both
 * account management and transaction retrieval.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionsService transactionsService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID accountId;
    private UUID clientId;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
        accountId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        testAccountId = UUID.randomUUID();
    }

    // ==================== GET RECENT TRANSACTIONS TESTS ====================

    @Test
    @DisplayName("Get recent transactions successfully")
    void testGetRecentTransactions_Success() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "WITHDRAWAL",
                        new BigDecimal("1000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("DEPOSIT", response.getBody().get(0).getType());
        assertEquals("WITHDRAWAL", response.getBody().get(1).getType());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with default limit")
    void testGetRecentTransactions_DefaultLimit() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with custom limit")
    void testGetRecentTransactions_CustomLimit() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("2000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "WITHDRAWAL",
                        new BigDecimal("1000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(2)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 5))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 5);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 5);
    }

    @Test
    @DisplayName("Get recent transactions with empty list")
    void testGetRecentTransactions_EmptyList() {
        // Arrange
        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions account not found")
    void testGetRecentTransactions_AccountNotFound() {
        // Arrange
        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenThrow(new IllegalArgumentException("Account not found: " + testAccountId));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> accountController.getRecentTransactions(testAccountId, 10));
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with limit of 1")
    void testGetRecentTransactions_LimitOne() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 1))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions with large limit")
    void testGetRecentTransactions_LargeLimit() {
        // Arrange - Create 50 transactions
        List<AccountsResponse.TransactionItem> mockTransactions = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mockTransactions.add(new AccountsResponse.TransactionItem(
                    UUID.randomUUID(),
                    i % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL",
                    new BigDecimal((i + 1) * 100),
                    OffsetDateTime.now(ZoneOffset.UTC).minusHours(i)
            ));
        }

        when(transactionsService.getRecentTransactions(testAccountId, 100))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 100);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions verifies transaction data")
    void testGetRecentTransactions_VerifyData() {
        // Arrange
        UUID txnId1 = UUID.randomUUID();
        UUID txnId2 = UUID.randomUUID();
        BigDecimal amount1 = new BigDecimal("5000.00");
        BigDecimal amount2 = new BigDecimal("1000.00");
        OffsetDateTime time1 = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime time2 = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);

        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(txnId1, "DEPOSIT", amount1, time1),
                new AccountsResponse.TransactionItem(txnId2, "WITHDRAWAL", amount2, time2)
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(txnId1, response.getBody().get(0).getTransactionId());
        assertEquals("DEPOSIT", response.getBody().get(0).getType());
        assertEquals(amount1, response.getBody().get(0).getAmount());
        assertEquals(time1, response.getBody().get(0).getCreatedAt());

        assertEquals(txnId2, response.getBody().get(1).getTransactionId());
        assertEquals("WITHDRAWAL", response.getBody().get(1).getType());
        assertEquals(amount2, response.getBody().get(1).getAmount());
        assertEquals(time2, response.getBody().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("Get recent transactions response type is OK")
    void testGetRecentTransactions_ResponseType() {
        // Arrange
        when(transactionsService.getRecentTransactions(any(UUID.class), anyInt()))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== LIST ACCOUNTS TESTS ====================

    @Test
    @DisplayName("Should list all accounts and return 200 OK")
    void testListAccounts_Success() throws Exception {
        AccountsResponse.AccountListItem account1 = new AccountsResponse.AccountListItem();
        AccountsResponse.AccountListItem account2 = new AccountsResponse.AccountListItem();

        List<AccountsResponse.AccountListItem> accounts = new ArrayList<>();
        accounts.add(account1);
        accounts.add(account2);

        when(accountService.listAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(accountService, times(1)).listAccounts();
    }

    @Test
    @DisplayName("Should list accounts with empty result")
    void testListAccounts_Empty() throws Exception {
        when(accountService.listAccounts()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountService).listAccounts();
    }

    @Test
    @DisplayName("Should call service method once for list accounts")
    void testListAccounts_ServiceMethodCalledOnce() throws Exception {
        when(accountService.listAccounts()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/accounts"));

        verify(accountService).listAccounts();
    }

    // ==================== CREATE ACCOUNT TESTS ====================

    @Test
    @DisplayName("Should create account and return 201 CREATED")
    void testCreateAccount_Success() throws Exception {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(clientId);
        request.setName("New Account");
        request.setInitialCashBalance(new BigDecimal("10000.00"));

        AccountsResponse.Account response = new AccountsResponse.Account();
        when(accountService.createAccount(any(AccountsRequest.CreateAccount.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(accountService, times(1)).createAccount(any(AccountsRequest.CreateAccount.class));
    }

    @Test
    @DisplayName("Should call service method once for create account")
    void testCreateAccount_ServiceMethodCalledOnce() throws Exception {
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(clientId);
        request.setName("Test Account");
        request.setInitialCashBalance(new BigDecimal("5000.00"));

        AccountsResponse.Account response = new AccountsResponse.Account();
        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(accountService).createAccount(any(AccountsRequest.CreateAccount.class));
    }

    // ==================== GET ACCOUNT DETAILS TESTS ====================

    @Test
    @DisplayName("Should get account details by ID and return 200 OK")
    void testGetAccountDetails_Success() throws Exception {
        AccountsResponse.AccountDetail response = new AccountsResponse.AccountDetail();
        when(accountService.getAccountDetails(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk());

        verify(accountService, times(1)).getAccountDetails(accountId);
    }

    @Test
    @DisplayName("Should call service method once for get account details")
    void testGetAccountDetails_ServiceMethodCalledOnce() throws Exception {
        AccountsResponse.AccountDetail response = new AccountsResponse.AccountDetail();
        when(accountService.getAccountDetails(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}", accountId));

        verify(accountService).getAccountDetails(accountId);
    }

    // ==================== GET ACCOUNT SUMMARY TESTS ====================

    @Test
    @DisplayName("Should get account summary and return 200 OK")
    void testGetAccountSummary_Success() throws Exception {
        AccountsResponse.Summary response = new AccountsResponse.Summary();
        when(accountService.getAccountSummary(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/summary", accountId))
                .andExpect(status().isOk());

        verify(accountService, times(1)).getAccountSummary(accountId);
    }

    @Test
    @DisplayName("Should call service method once for get account summary")
    void testGetAccountSummary_ServiceMethodCalledOnce() throws Exception {
        AccountsResponse.Summary response = new AccountsResponse.Summary();
        when(accountService.getAccountSummary(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/summary", accountId));

        verify(accountService).getAccountSummary(accountId);
    }

    // ==================== GET ACCOUNT PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Should get account performance with default period and return 200 OK")
    void testGetAccountPerformance_DefaultPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "1Y")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance", accountId))
                .andExpect(status().isOk());

        verify(accountService, times(1)).getAccountPerformance(accountId, "1Y");
    }

    @Test
    @DisplayName("Should get account performance with custom period")
    void testGetAccountPerformance_CustomPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();

        when(accountService.getAccountPerformance(accountId, "3M")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=3M", accountId))
                .andExpect(status().isOk());

        verify(accountService).getAccountPerformance(accountId, "3M");
    }

    @Test
    @DisplayName("Should call service with 1D period")
    void testGetAccountPerformance_OneDayPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "1D")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=1D", accountId));

        verify(accountService).getAccountPerformance(accountId, "1D");
    }

    @Test
    @DisplayName("Should call service with 1W period")
    void testGetAccountPerformance_OneWeekPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "1W")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=1W", accountId));

        verify(accountService).getAccountPerformance(accountId, "1W");
    }

    @Test
    @DisplayName("Should call service with 1M period")
    void testGetAccountPerformance_OneMonthPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "1M")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=1M", accountId));

        verify(accountService).getAccountPerformance(accountId, "1M");
    }

    @Test
    @DisplayName("Should call service with 6M period")
    void testGetAccountPerformance_SixMonthPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "6M")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=6M", accountId));

        verify(accountService).getAccountPerformance(accountId, "6M");
    }

    @Test
    @DisplayName("Should call service with ALL period")
    void testGetAccountPerformance_AllPeriod() throws Exception {
        AccountsResponse.Performance response = new AccountsResponse.Performance();
        when(accountService.getAccountPerformance(accountId, "ALL")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}/performance?period=ALL", accountId));

        verify(accountService).getAccountPerformance(accountId, "ALL");
    }

    // ==================== UPDATE ACCOUNT TESTS ====================

    @Test
    @DisplayName("Should update account and return 200 OK")
    void testUpdateAccount_Success() throws Exception {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        request.setName("Updated Account Name");

        AccountsResponse.Account response = new AccountsResponse.Account();

        when(accountService.updateAccount(eq(accountId), any(AccountsRequest.UpdateAccount.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService, times(1)).updateAccount(eq(accountId), any(AccountsRequest.UpdateAccount.class));
    }

    @Test
    @DisplayName("Should call service method once for update account")
    void testUpdateAccount_ServiceMethodCalledOnce() throws Exception {
        AccountsRequest.UpdateAccount request = new AccountsRequest.UpdateAccount();
        request.setName("New Name");

        AccountsResponse.Account response = new AccountsResponse.Account();
        when(accountService.updateAccount(eq(accountId), any())).thenReturn(response);

        mockMvc.perform(put("/api/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(accountService).updateAccount(eq(accountId), any(AccountsRequest.UpdateAccount.class));
    }

    // ==================== DEPOSIT CASH TESTS ====================

    @Test
    @DisplayName("Should deposit cash and return 200 OK")
    void testDepositCash_Success() throws Exception {
        AccountsRequest.Deposit request = new AccountsRequest.Deposit();
        request.setAmount(new BigDecimal("5000.00"));

        AccountsResponse.Transaction response = new AccountsResponse.Transaction();
        when(accountService.depositCash(accountId, new BigDecimal("5000.00")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/accounts/{accountId}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService, times(1)).depositCash(eq(accountId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should call service method once for deposit cash")
    void testDepositCash_ServiceMethodCalledOnce() throws Exception {
        AccountsRequest.Deposit request = new AccountsRequest.Deposit();
        request.setAmount(new BigDecimal("2000.00"));

        AccountsResponse.Transaction response = new AccountsResponse.Transaction();
        when(accountService.depositCash(eq(accountId), any(BigDecimal.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/accounts/{accountId}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(accountService).depositCash(eq(accountId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should handle various deposit amounts")
    void testDepositCash_VariousAmounts() throws Exception {
        BigDecimal[] amounts = {
            new BigDecimal("100.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("999.99"),
            new BigDecimal("50000.00")
        };

        for (BigDecimal amount : amounts) {
            AccountsRequest.Deposit request = new AccountsRequest.Deposit();
            request.setAmount(amount);

            AccountsResponse.Transaction response = new AccountsResponse.Transaction();
            when(accountService.depositCash(eq(accountId), eq(amount)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/accounts/{accountId}/deposit", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(accountService).depositCash(eq(accountId), eq(amount));
        }
    }

    // ==================== WITHDRAW CASH TESTS ====================

    @Test
    @DisplayName("Should withdraw cash and return 200 OK")
    void testWithdrawCash_Success() throws Exception {
        AccountsRequest.Withdrawal request = new AccountsRequest.Withdrawal();
        request.setAmount(new BigDecimal("3000.00"));

        AccountsResponse.Transaction response = new AccountsResponse.Transaction();
        when(accountService.withdrawCash(accountId, new BigDecimal("3000.00")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/accounts/{accountId}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService, times(1)).withdrawCash(eq(accountId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should call service method once for withdraw cash")
    void testWithdrawCash_ServiceMethodCalledOnce() throws Exception {
        AccountsRequest.Withdrawal request = new AccountsRequest.Withdrawal();
        request.setAmount(new BigDecimal("1500.00"));

        AccountsResponse.Transaction response = new AccountsResponse.Transaction();
        when(accountService.withdrawCash(eq(accountId), any(BigDecimal.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/accounts/{accountId}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(accountService).withdrawCash(eq(accountId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should handle various withdrawal amounts")
    void testWithdrawCash_VariousAmounts() throws Exception {
        BigDecimal[] amounts = {
            new BigDecimal("50.00"),
            new BigDecimal("500.00"),
            new BigDecimal("2500.50"),
            new BigDecimal("10000.00")
        };

        for (BigDecimal amount : amounts) {
            AccountsRequest.Withdrawal request = new AccountsRequest.Withdrawal();
            request.setAmount(amount);

            AccountsResponse.Transaction response = new AccountsResponse.Transaction();
            when(accountService.withdrawCash(eq(accountId), eq(amount)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/accounts/{accountId}/withdraw", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(accountService).withdrawCash(eq(accountId), eq(amount));
        }
    }

    // ==================== GENERAL ENDPOINT TESTS ====================

    @Test
    @DisplayName("All endpoints should respond with content type JSON")
    void testAllEndpoints_ResponseContentType() throws Exception {
        when(accountService.listAccounts()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should handle correct HTTP methods")
    void testCorrectHttpMethods() throws Exception {
        when(accountService.listAccounts()).thenReturn(new ArrayList<>());

        // GET should work
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().is2xxSuccessful());

        // POST should also work (not 405)
        AccountsRequest.CreateAccount request = new AccountsRequest.CreateAccount();
        request.setClientId(clientId);
        request.setName("Test");
        request.setInitialCashBalance(new BigDecimal("1000"));
        
        AccountsResponse.Account response = new AccountsResponse.Account();
        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
