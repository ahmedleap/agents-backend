package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthSession;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.AuthRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final long ACCESS_TOKEN_SECONDS = 900;
    private static final int ACCESS_TOKEN_MINUTES = 15;
    private static final int REFRESH_TOKEN_DAYS = 30;
    private static final int INACTIVITY_MINUTES = 30;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthModels.MessageResponse register(AuthModels.RegisterRequest request, String ip, String device) {
        if (request == null) throw badRequest("Registration details are required.");
        String email = normalizeEmail(request.email());
        validatePassword(request.password());
        if (blank(request.firstName()) || blank(request.lastName()) || request.dateOfBirth() == null) {
            throw badRequest("First name, last name, and date of birth are required.");
        }
        if (request.dateOfBirth().isAfter(LocalDate.now())) throw badRequest("Date of birth must be in the past.");
        String country = request.country() == null ? null : request.country().trim().toUpperCase(Locale.ROOT);
        if (country == null || !country.matches("^[A-Z]{2}$")) throw badRequest("Country must be a two-letter ISO code.");
        String phone = request.phone() == null || request.phone().isBlank() ? null : request.phone().trim();
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw badRequest("Phone must use international E.164 format, for example +353123456789.");
        }
        LocalDateTime now = LocalDateTime.now();
        UUID clientId = UUID.randomUUID();
        try {
            repository.insertClient(clientId, request.firstName().trim(), request.lastName().trim(), email,
                    passwordEncoder.encode(request.password()), phone, country, request.dateOfBirth(), now,
                    ip == null ? "" : ip, truncate(device, 512));
            repository.insertInitialAccount(UUID.randomUUID(), clientId, now);
        } catch (DuplicateKeyException exception) {
            throw new AuthException(HttpStatus.CONFLICT, "An account with that email or phone already exists.");
        }
        return new AuthModels.MessageResponse("Account created successfully.");
    }

    @Transactional(noRollbackFor = AuthException.class)
    public AuthModels.TokenResponse login(AuthModels.LoginRequest request, String device) {
        if (request == null || blank(request.password())) throw unauthorized();
        String email = normalizeEmail(request.email());
        AuthUser user = repository.findByEmail(email).orElseThrow(AuthService::unauthorized);
        if ("LOCKED".equals(user.getAuthStatus()) && user.getLockedUntil() != null
                && !user.getLockedUntil().isAfter(LocalDateTime.now())) {
            repository.unlockIfExpired(user.getClientId());
            user = repository.findById(user.getClientId()).orElseThrow(AuthService::unauthorized);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            repository.recordFailedLogin(user.getClientId());
            throw unauthorized();
        }
        if (!"ACTIVE".equals(user.getAuthStatus())) {
            if ("LOCKED".equals(user.getAuthStatus())) {
                throw new AuthException(HttpStatus.LOCKED, "Account is locked. Try again after 30 minutes.");
            }
            throw new AuthException(HttpStatus.FORBIDDEN, "This account is not active.");
        }
        repository.clearFailedLogins(user.getClientId());
        LocalDateTime now = LocalDateTime.now();
        String access = newOpaqueToken();
        String refresh = newOpaqueToken();
        AuthSession session = new AuthSession();
        session.setSessionId(UUID.randomUUID());
        session.setClientId(user.getClientId());
        session.setAccessTokenHash(sha256(access));
        session.setRefreshTokenHash(sha256(refresh));
        session.setCreatedAt(now);
        session.setLastActive(now);
        session.setAccessExpiresAt(now.plusMinutes(ACCESS_TOKEN_MINUTES));
        session.setRefreshExpiresAt(now.plusDays(REFRESH_TOKEN_DAYS));
        session.setDevice(truncate(device, 512));
        repository.insertSession(session);
        return new AuthModels.TokenResponse(access, refresh, ACCESS_TOKEN_SECONDS, false);
    }

    @Transactional
    public AuthModels.TokenResponse refresh(String refreshToken) {
        if (blank(refreshToken)) throw unauthorized();
        AuthSession session = repository.findByRefreshHash(sha256(refreshToken)).orElseThrow(AuthService::unauthorized);
        validateSession(session, true);
        AuthUser user = repository.findById(session.getClientId()).orElseThrow(AuthService::unauthorized);
        if (!"ACTIVE".equals(user.getAuthStatus())) throw unauthorized();
        String newAccess = newOpaqueToken();
        String newRefresh = newOpaqueToken();
        LocalDateTime now = LocalDateTime.now();
        int changed = repository.rotateSession(session.getSessionId(), session.getRefreshTokenHash(),
                sha256(newAccess), sha256(newRefresh), now.plusMinutes(ACCESS_TOKEN_MINUTES),
                now.plusDays(REFRESH_TOKEN_DAYS));
        if (changed != 1) throw unauthorized();
        return new AuthModels.TokenResponse(newAccess, newRefresh, ACCESS_TOKEN_SECONDS, false);
    }

    @Transactional
    public UUID authenticate(String accessToken) {
        if (blank(accessToken)) throw unauthorized();
        AuthSession session = repository.findByAccessHash(sha256(accessToken)).orElseThrow(AuthService::unauthorized);
        validateSession(session, false);
        AuthUser user = repository.findById(session.getClientId()).orElseThrow(AuthService::unauthorized);
        if (!"ACTIVE".equals(user.getAuthStatus())) throw unauthorized();
        if (repository.touchSession(session.getSessionId()) != 1) throw unauthorized();
        return user.getClientId();
    }

    @Transactional
    public void logout(String accessToken) {
        if (!blank(accessToken)) repository.revokeByAccessHash(sha256(accessToken));
    }

    @Transactional
    public void logoutAll(UUID clientId) {
        repository.revokeAllSessions(clientId);
    }

    @Transactional
    public List<AuthModels.SessionResponse> sessions(UUID clientId) {
        return repository.findActiveSessions(clientId).stream()
                .map(session -> new AuthModels.SessionResponse(session.getSessionId(), session.getDevice(),
                        session.getLocation(), session.getCreatedAt(), session.getLastActive())).toList();
    }

    @Transactional(readOnly = true)
    public AuthModels.UserResponse currentUser(UUID clientId) {
        AuthUser user = repository.findById(clientId).orElseThrow(AuthService::unauthorized);
        List<Map<String, Object>> accounts = repository.findAccounts(clientId);
        return new AuthModels.UserResponse(user.getClientId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getCountry(), user.isEmailVerified(), user.getDateOfBirth(), user.getJoinDate(), accounts);
    }

    private void validateSession(AuthSession session, boolean refresh) {
        LocalDateTime now = LocalDateTime.now();
        if (session.getRevokedAt() != null || !session.getRefreshExpiresAt().isAfter(now)
                || !session.getLastActive().isAfter(now.minusMinutes(INACTIVITY_MINUTES))) throw unauthorized();
        if (!refresh && !session.getAccessExpiresAt().isAfter(now)) throw unauthorized();
    }

    private String normalizeEmail(String email) {
        if (blank(email)) throw badRequest("A valid email address is required.");
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 255 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw badRequest("A valid email address is required.");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*")
                || !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*")) {
            throw badRequest("Password must be 12-72 characters and include uppercase, lowercase, a number, and a special character.");
        }
    }

    private String newOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private static AuthException unauthorized() { return new AuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials or session."); }
    private static AuthException badRequest(String message) { return new AuthException(HttpStatus.BAD_REQUEST, message); }
}
