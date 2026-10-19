package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.AdminAccountResponse;
import com.agentsbackend.DTO.response.AdminAuditResponse;
import com.agentsbackend.DTO.response.AdminClientResponse;
import com.agentsbackend.DTO.response.AdminInsightsResponse;
import com.agentsbackend.DTO.response.AdminOrderResponse;
import com.agentsbackend.DTO.response.TradingActivityReportResponse;
import com.agentsbackend.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/admin", "/api/v1/admin"})
public class AdminReadController {

    private final AdminService adminService;

    public AdminReadController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<AdminClientResponse>> getClients() {
        return ResponseEntity.ok(adminService.getClients());
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<AdminClientResponse> getClient(@PathVariable("clientId") UUID clientId) {
        return ResponseEntity.ok(adminService.getClient(clientId));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AdminAccountResponse>> getAccounts() {
        return ResponseEntity.ok(adminService.getAccounts());
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AdminAccountResponse> getAccount(@PathVariable("accountId") UUID accountId) {
        return ResponseEntity.ok(adminService.getAccount(accountId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderResponse>> getOrders() {
        return ResponseEntity.ok(adminService.getOrders());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<AdminOrderResponse> getOrder(@PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(adminService.getOrder(orderId));
    }

    @GetMapping("/trades/{tradeId}")
    public ResponseEntity<AdminOrderResponse> getTrade(@PathVariable("tradeId") UUID tradeId) {
        return ResponseEntity.ok(adminService.getTrade(tradeId));
    }

    @GetMapping("/trades/{tradeId}/audit")
    public ResponseEntity<List<AdminAuditResponse>> getTradeAudit(@PathVariable("tradeId") UUID tradeId) {
        return ResponseEntity.ok(adminService.getTradeAudit(tradeId));
    }

    @GetMapping("/reports/tradingactivity")
    public ResponseEntity<TradingActivityReportResponse> getTradingActivityReport() {
        return ResponseEntity.ok(adminService.getTradingActivityReport());
    }

    @GetMapping("/insights")
    public ResponseEntity<AdminInsightsResponse> getInsights() {
        return ResponseEntity.ok(adminService.getInsights());
    }
}