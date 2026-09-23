package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CashMovementRequest;
import com.agentsbackend.DTO.response.AccountBalanceResponse;
import com.agentsbackend.services.CashManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CashManagementControllerTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("950e8400-e29b-41d4-a716-446655880001");

    private MockMvc mockMvc;

    @Mock
    private CashManagementService cashManagementService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new CashManagementController(cashManagementService))
                .setValidator(validator)
                .build();
    }

    @Test
    void getBalanceReturnsAccountBalance() throws Exception {
        when(cashManagementService.getBalance(ACCOUNT_ID))
                .thenReturn(new AccountBalanceResponse(ACCOUNT_ID, new BigDecimal("75000.00")));

        mockMvc.perform(get("/api/cash/accounts/{accountId}/balance", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.cashBalance").value(75000.00));
    }

    @Test
    void depositReturnsUpdatedBalance() throws Exception {
        when(cashManagementService.deposit(eq(ACCOUNT_ID), any(CashMovementRequest.class)))
                .thenReturn(new AccountBalanceResponse(ACCOUNT_ID, new BigDecimal("77500.00")));

        mockMvc.perform(post("/api/cash/accounts/{accountId}/deposit", ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 2500.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashBalance").value(77500.00));
    }
}
