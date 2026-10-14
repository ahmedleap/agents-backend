package com.agentsbackend.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an order cannot be found.
 */
public class OrderNotFoundException extends RuntimeException {
    private UUID orderId;
    
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
    }

    public OrderNotFoundException(String message, UUID orderId) {
        super(message);
        this.orderId = orderId;
    }

    public OrderNotFoundException(String message, Throwable cause) {
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
