package com.agentsbackend.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AuthModels {
    private AuthModels() {}

    public record RegisterRequest(String firstName, String lastName, LocalDate dateOfBirth,
                                  String email, String password, String country, String phone) {}
    public record LoginRequest(String email, String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record EmailTokenRequest(String token) {}
    public record ResetRequest(String email) {}
    public record ResetPasswordRequest(String token, String newPassword) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("refresh_token") String refreshToken,
                                @JsonProperty("expires_in") long expiresIn,
                                @JsonProperty("mfa_required") boolean mfaRequired) {}
    public record MessageResponse(String message) {}
    public record SessionResponse(@JsonProperty("session_id") UUID sessionId,
                                  String device, String location,
                                  @JsonProperty("created_at") LocalDateTime createdAt,
                                  @JsonProperty("last_active") LocalDateTime lastActive) {}
    public record UserResponse(@JsonProperty("client_id") UUID clientId,
                               String firstName, String lastName, String email, String phone,
                               String country, @JsonProperty("email_verified") boolean emailVerified,
                               @JsonProperty("date_of_birth") LocalDate dateOfBirth,
                               @JsonProperty("join_date") LocalDateTime joinDate,
                               List<Map<String, Object>> accounts) {}
}
