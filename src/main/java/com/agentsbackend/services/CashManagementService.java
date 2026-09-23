package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.CashMovementRequest;
import com.agentsbackend.DTO.response.AccountBalanceResponse;

import java.util.UUID;

public interface CashManagementService {

    AccountBalanceResponse getBalance(UUID accountId);

    AccountBalanceResponse deposit(UUID accountId, CashMovementRequest request);

    AccountBalanceResponse withdraw(UUID accountId, CashMovementRequest request);
}
