package com.agentsbackend.exceptions;

/**
 * Exception thrown when an order operation fails due to invalid order status.
 */
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
