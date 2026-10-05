package com.agentsbackend.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an order operation fails due to invalid order status.
 */
public class InvalidOrderStatusException extends RuntimeException {
    private UUID orderId;
    
    public InvalidOrderStatusException(String message) {
        super(message);
        this.orderId = null;
    }

    public InvalidOrderStatusException(String message, UUID orderId) {
        super(message);
        this.orderId = orderId;
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
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
