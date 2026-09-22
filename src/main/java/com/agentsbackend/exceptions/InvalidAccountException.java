package com.agentsbackend.exceptions;

/**
 * Exception thrown when an account is invalid or not found.
 */
public class InvalidAccountException extends RuntimeException {
    public InvalidAccountException(String message) {
        super(message);
    }

    public InvalidAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
