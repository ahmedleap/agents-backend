package com.agentsbackend.exceptions;

import java.util.UUID;

/**
 * Exception thrown when account is invalid or not found.
 */
public class InvalidAccountException extends RuntimeException {
    private UUID orderId;
    
    public InvalidAccountException(String message) {
        super(message);
        this.orderId = null;
    }

    public InvalidAccountException(String message, UUID orderId) {
        super(message);
        this.orderId = orderId;
    }

    public InvalidAccountException(String message, Throwable cause) {
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
