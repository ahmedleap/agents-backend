package com.agentsbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class NewsFeedException extends RuntimeException {

    public NewsFeedException(String message, Throwable cause) {
        super(message, cause);
    }
}