package com.agentsbackend.exceptions;

/**
 * Exception thrown when order parameters are invalid.
 */
public class InvalidOrderParametersException extends RuntimeException {
    public InvalidOrderParametersException(String message) {
        super(message);
    }

    public InvalidOrderParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}
