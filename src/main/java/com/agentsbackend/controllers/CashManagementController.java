package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CashMovementRequest;
import com.agentsbackend.DTO.response.AccountBalanceResponse;
import com.agentsbackend.services.CashManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/cash")
public class CashManagementController {

    private final CashManagementService cashManagementService;

    public CashManagementController(CashManagementService cashManagementService) {
        this.cashManagementService = cashManagementService;
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(@PathVariable UUID accountId) {
        return ResponseEntity.ok(cashManagementService.getBalance(accountId));
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(@PathVariable UUID accountId,
                                                        @Valid @RequestBody CashMovementRequest request) {
        return ResponseEntity.ok(cashManagementService.deposit(accountId, request));
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<AccountBalanceResponse> withdraw(@PathVariable UUID accountId,
                                                         @Valid @RequestBody CashMovementRequest request) {
        return ResponseEntity.ok(cashManagementService.withdraw(accountId, request));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
