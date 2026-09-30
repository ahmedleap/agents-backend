package com.agentsbackend.exceptions;

import java.util.UUID;

/**
 * Exception thrown when account doesn't have sufficient funds.
 */
public class InsufficientFundsException extends RuntimeException {
    private UUID orderId;
    
    public InsufficientFundsException(String message) {
        super(message);
        this.orderId = null;
    }

    public InsufficientFundsException(String message, UUID orderId) {
        super(message);
        this.orderId = orderId;
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
