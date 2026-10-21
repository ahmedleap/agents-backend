package com.agentsbackend.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuthSession {
    private UUID sessionId;
    private UUID clientId;
    private String accessTokenHash;
    private String refreshTokenHash;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActive;
    private OffsetDateTime accessExpiresAt;
    private OffsetDateTime refreshExpiresAt;
    private OffsetDateTime revokedAt;
    private String device;

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public String getAccessTokenHash() { return accessTokenHash; }
    public void setAccessTokenHash(String accessTokenHash) { this.accessTokenHash = accessTokenHash; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getLastActive() { return lastActive; }
    public void setLastActive(OffsetDateTime lastActive) { this.lastActive = lastActive; }
    public OffsetDateTime getAccessExpiresAt() { return accessExpiresAt; }
    public void setAccessExpiresAt(OffsetDateTime accessExpiresAt) { this.accessExpiresAt = accessExpiresAt; }
    public OffsetDateTime getRefreshExpiresAt() { return refreshExpiresAt; }
    public void setRefreshExpiresAt(OffsetDateTime refreshExpiresAt) { this.refreshExpiresAt = refreshExpiresAt; }
    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(OffsetDateTime revokedAt) { this.revokedAt = revokedAt; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
}
