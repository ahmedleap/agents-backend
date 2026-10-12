package com.agentsbackend.services;

/**
 * Exception thrown when JWT verification fails.
 */
public class TokenVerificationException extends RuntimeException {
    private final String code;

    public TokenVerificationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public TokenVerificationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
