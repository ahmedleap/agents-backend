package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.time.LocalDateTime;
import com.agentsbackend.enums.OrderStatus;

/**
 * Response DTO for cancelled order confirmation.
 */
public class CancelOrderResponse {

    private UUID orderId;
    private OrderStatus status;
    private LocalDateTime cancelledAt;
    private String message;

    public CancelOrderResponse() {
    }

    public CancelOrderResponse(UUID orderId, OrderStatus status, LocalDateTime cancelledAt, String message) {
        this.orderId = orderId;
        this.status = status;
        this.cancelledAt = cancelledAt;
        this.message = message;
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
