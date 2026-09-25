package com.agentsbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(name = "audit_log_id", columnDefinition = "UUID")
    private UUID auditLogId;

    @Column(name = "order_id", columnDefinition = "UUID", nullable = false)
    private UUID orderId;

    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @Column(name = "client_id", columnDefinition = "UUID", nullable = false)
    private UUID clientId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "details", columnDefinition = "JSONB")
    private String details;

    @Column(name = "created_at", nullable = false)
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
