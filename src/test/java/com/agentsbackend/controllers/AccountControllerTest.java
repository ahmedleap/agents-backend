package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.AccountsRequest;
import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.services.AccountService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID accountId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
        accountId = UUID.randomUUID();
        clientId = UUID.randomUUID();
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
