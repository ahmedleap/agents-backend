package com.agentsbackend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthSession {
    private UUID sessionId;
    private UUID clientId;
    private String accessTokenHash;
    private String refreshTokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;
    private LocalDateTime accessExpiresAt;
    private LocalDateTime refreshExpiresAt;
    private LocalDateTime revokedAt;
    private String device;
    private String location;

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public String getAccessTokenHash() { return accessTokenHash; }
    public void setAccessTokenHash(String accessTokenHash) { this.accessTokenHash = accessTokenHash; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
    public LocalDateTime getAccessExpiresAt() { return accessExpiresAt; }
    public void setAccessExpiresAt(LocalDateTime accessExpiresAt) { this.accessExpiresAt = accessExpiresAt; }
    public LocalDateTime getRefreshExpiresAt() { return refreshExpiresAt; }
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) { this.refreshExpiresAt = refreshExpiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
