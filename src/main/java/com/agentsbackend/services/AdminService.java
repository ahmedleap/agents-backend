package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.DTO.requests.CreateAdminRequest;
import com.agentsbackend.DTO.response.AdminAccountResponse;
import com.agentsbackend.DTO.response.AdminAuditResponse;
import com.agentsbackend.DTO.response.AdminClientResponse;
import com.agentsbackend.DTO.response.AdminInsightsResponse;
import com.agentsbackend.DTO.response.AdminOrderResponse;
import com.agentsbackend.DTO.response.TradingActivityReportResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    // Creates and persists a new admin record
    Admin createAdmin(Admin admin);

    // Creates an admin from an API request, hashing the supplied password.
    Admin createAdmin(CreateAdminRequest request);

    List<AdminClientResponse> getClients();

    AdminClientResponse getClient(UUID clientId);

    List<AdminAccountResponse> getAccounts();

    AdminAccountResponse getAccount(UUID accountId);

    List<AdminOrderResponse> getOrders();

    AdminOrderResponse getOrder(UUID orderId);

    AdminOrderResponse getTrade(UUID tradeId);

    List<AdminAuditResponse> getTradeAudit(UUID tradeId);

    TradingActivityReportResponse getTradingActivityReport();

    AdminInsightsResponse getInsights();
}
