package com.agentsbackend.controllers;

import com.agentsbackend.services.AuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<AuthModels.MessageResponse> handleAuthException(AuthException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new AuthModels.MessageResponse(exception.getMessage()));
    }
}
