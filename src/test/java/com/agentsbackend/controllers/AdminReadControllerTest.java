package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.AdminClientResponse;
import com.agentsbackend.DTO.response.AdminAccountResponse;
import com.agentsbackend.DTO.response.AdminAuditResponse;
import com.agentsbackend.DTO.response.AdminInsightsResponse;
import com.agentsbackend.DTO.response.AdminOrderResponse;
import com.agentsbackend.DTO.response.TradingActivityReportResponse;
import com.agentsbackend.services.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminReadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminReadController(adminService)).build();
    }

    @Test
    void getClientsReturnsClientList() throws Exception {
        UUID clientId = UUID.randomUUID();
        AdminClientResponse client = new AdminClientResponse();
        client.setClientId(clientId);
        client.setClientName("First Middle Last");
        client.setEmail("client@example.com");
        when(adminService.getClients()).thenReturn(List.of(client));

        mockMvc.perform(get("/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(clientId.toString()))
                .andExpect(jsonPath("$[0].clientName").value("First Middle Last"))
                .andExpect(jsonPath("$[0].email").value("client@example.com"));

        verify(adminService).getClients();
    }

    @Test
    void getOrderUsesOrderId() throws Exception {
        UUID orderId = UUID.randomUUID();
        AdminOrderResponse order = new AdminOrderResponse();
        order.setOrderId(orderId);
        when(adminService.getOrder(orderId)).thenReturn(order);

        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        verify(adminService).getOrder(orderId);
    }

    @Test
    void getClientUsesClientId() throws Exception {
        UUID clientId = UUID.randomUUID();
        AdminClientResponse client = new AdminClientResponse();
        client.setClientId(clientId);
        when(adminService.getClient(clientId)).thenReturn(client);

        mockMvc.perform(get("/api/v1/admin/clients/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()));

        verify(adminService).getClient(clientId);
    }

    @Test
    void getAccountsReturnsAccounts() throws Exception {
        AdminAccountResponse account = new AdminAccountResponse();
        account.setAccountId(UUID.randomUUID());
        account.setClientId(UUID.randomUUID());
        account.setClientName("First Last");
        when(adminService.getAccounts()).thenReturn(List.of(account));

        mockMvc.perform(get("/api/v1/admin/accounts"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountId").value(account.getAccountId().toString()))
                .andExpect(jsonPath("$[0].clientName").value("First Last"));

        verify(adminService).getAccounts();
    }

    @Test
    void getAccountUsesAccountId() throws Exception {
        UUID accountId = UUID.randomUUID();
        AdminAccountResponse account = new AdminAccountResponse();
        account.setAccountId(accountId);
        when(adminService.getAccount(accountId)).thenReturn(account);

        mockMvc.perform(get("/admin/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));

        verify(adminService).getAccount(accountId);
    }

    @Test
    void getOrdersReturnsOrders() throws Exception {
        AdminOrderResponse order = new AdminOrderResponse();
        order.setOrderId(UUID.randomUUID());
        order.setClientName("First Middle Last");
        when(adminService.getOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(order.getOrderId().toString()))
                .andExpect(jsonPath("$[0].clientName").value("First Middle Last"));

        verify(adminService).getOrders();
    }

    @Test
    void getTradeUsesTradeId() throws Exception {
        UUID tradeId = UUID.randomUUID();
        AdminOrderResponse trade = new AdminOrderResponse();
        trade.setOrderId(tradeId);
        trade.setClientName("First Last");
        when(adminService.getTrade(tradeId)).thenReturn(trade);

        mockMvc.perform(get("/api/v1/admin/trades/{tradeId}", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(tradeId.toString()))
                .andExpect(jsonPath("$.clientName").value("First Last"));

        verify(adminService).getTrade(tradeId);
    }

    @Test
    void getTradeAuditUsesTradeId() throws Exception {
        UUID tradeId = UUID.randomUUID();
        AdminAuditResponse audit = new AdminAuditResponse();
        audit.setOrderId(tradeId);
        audit.setClientName("First Last");
        when(adminService.getTradeAudit(tradeId)).thenReturn(List.of(audit));

        mockMvc.perform(get("/admin/trades/{tradeId}/audit", tradeId))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderId").value(tradeId.toString()))
                .andExpect(jsonPath("$[0].clientName").value("First Last"));

        verify(adminService).getTradeAudit(tradeId);
    }

    @Test
    void getTradingActivityReportReturnsReport() throws Exception {
        TradingActivityReportResponse report = new TradingActivityReportResponse(
                4L, 2L, 1L, 1L, new BigDecimal("3"), new BigDecimal("150.50"));
        when(adminService.getTradingActivityReport()).thenReturn(report);

        mockMvc.perform(get("/api/v1/admin/reports/tradingactivity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(4))
                .andExpect(jsonPath("$.filledOrders").value(2))
                .andExpect(jsonPath("$.tradedValue").value(150.50));

        verify(adminService).getTradingActivityReport();
    }

    @Test
    void getInsightsReturnsInsights() throws Exception {
        AdminInsightsResponse insights = new AdminInsightsResponse(
                3L, 4L, 5L, 2L, new BigDecimal("99.25"));
        when(adminService.getInsights()).thenReturn(insights);

        mockMvc.perform(get("/admin/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientCount").value(3))
                .andExpect(jsonPath("$.accountCount").value(4))
                .andExpect(jsonPath("$.tradedValue").value(99.25));

        verify(adminService).getInsights();
    }
}