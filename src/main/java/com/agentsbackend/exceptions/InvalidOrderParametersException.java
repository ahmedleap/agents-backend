package com.agentsbackend.exceptions;

import java.util.UUID;

/**
 * Exception thrown when order parameters are invalid.
 */
public class InvalidOrderParametersException extends RuntimeException {
    private UUID orderId;
    
    public InvalidOrderParametersException(String message) {
        super(message);
        this.orderId = null;
    }

    public InvalidOrderParametersException(String message, UUID orderId) {
        super(message);
        this.orderId = orderId;
    }

    public InvalidOrderParametersException(String message, Throwable cause) {
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
