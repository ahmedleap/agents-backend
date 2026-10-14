package com.agentsbackend.exceptions;

import java.util.UUID;

public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException(UUID clientId, UUID accountId) {
        super("Account with ID: " + accountId + " does not belong to client with ID: " + clientId);
    }
}
