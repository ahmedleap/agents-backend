package com.agentsbackend.exceptions;

import java.util.UUID;

public class HoldingNotFoundException extends RuntimeException {
    public HoldingNotFoundException(UUID holdingId) {
        super("Holding not found with ID: " + holdingId);
    }
}
