package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for cancelling an order.
 */
public class CancelOrderRequest {

    @NotNull(message = "Order ID cannot be null")
    private UUID orderId;

    private String cancellationReason;

    public CancelOrderRequest() {
    }

    public CancelOrderRequest(UUID orderId, String cancellationReason) {
        this.orderId = orderId;
        this.cancellationReason = cancellationReason;
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
