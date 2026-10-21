
package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.transactional.AuthRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthRepository repository;

    @Value("${auth.session.inactivity-minutes:30}")
    private int inactivityMinutes = 30;

    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    /** True when the access token's SHA-256 hash has a live session owned by clientId; refreshes last_active. */
    @Transactional
    public boolean isSessionValid(String accessToken, UUID clientId) {
        if (accessToken == null || accessToken.isBlank() || clientId == null) {
            return false;
        }

        var session = repository.findByAccessHash(sha256(accessToken)).orElse(null);
        if (session == null || !clientId.equals(session.getClientId())) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (session.getRevokedAt() != null
                || session.getAccessExpiresAt() == null
                || !session.getAccessExpiresAt().isAfter(now)
                || session.getLastActive() == null
                || !session.getLastActive().isAfter(now.minusMinutes(inactivityMinutes))) {
            return false;
        }

        return repository.touchSession(session.getSessionId()) == 1;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    /**
     * Retrieves the authenticated client's profile
     * and associated trading accounts.
     *
     * The clientId must come from the subject (sub)
     * of a JWT already verified by Spring Security.
     */
    @Transactional(readOnly = true)
    public AuthModels.UserResponse currentUser(UUID clientId) {

        if (clientId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated client ID is required."
            );
        }

        AuthUser user = repository.findById(clientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Client not found."
            ));

        if (!"ACTIVE".equals(user.getAuthStatus())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Client account is not active."
            );
        }

        List<Map<String, Object>> accounts =
            repository.findAccounts(clientId);

        return new AuthModels.UserResponse(
            user.getClientId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhone(),
            user.getCountry(),
            user.isEmailVerified(),
            user.getDateOfBirth(),
            user.getJoinDate(),
            accounts
        );
    }
}
