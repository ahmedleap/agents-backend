package com.agentsbackend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLog {

    private UUID auditLogId;
    private UUID orderId;
    private UUID accountId;
    private UUID clientId;
    private String eventType;
    private LocalDateTime eventTime;
    private String reason;
    private String details;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(UUID auditLogId, UUID orderId, UUID accountId, UUID clientId, 
                    String eventType, LocalDateTime eventTime, String reason, 
                    String details, LocalDateTime createdAt) {
        this.auditLogId = auditLogId;
        this.orderId = orderId;
        this.accountId = accountId;
        this.clientId = clientId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.reason = reason;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(UUID auditLogId) {
        this.auditLogId = auditLogId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
