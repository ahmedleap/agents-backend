package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthSession;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final UUID CLIENT_ID = UUID.fromString("beefca11-0000-4000-8000-000000000001");
    private static final UUID SESSION_ID = UUID.fromString("beefca11-0000-4000-8000-000000000002");
    private static final String PASSWORD = "StrongPassword123!";
    private static final String ACCESS_TOKEN = "access-token-for-test";
    private static final String REFRESH_TOKEN = "refresh-token-for-test";

    @Mock private AuthRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, passwordEncoder);
    }

    @Test
    void registerCreatesClientAndInitialAccountWithoutEmailVerification() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("bcrypt-hash");

        AuthModels.MessageResponse response = service.register(validRegistration(), "127.0.0.1", "test-device");

        assertTrue(response.message().contains("created"));
        verify(repository).insertClient(any(UUID.class), eq("Alex"), eq("Example"), eq("alex@example.com"),
                eq("bcrypt-hash"), eq("+353123456789"), eq("IE"), eq(LocalDate.of(1990, 5, 20)),
                any(LocalDateTime.class), eq("127.0.0.1"), eq("test-device"));
        verify(repository).insertInitialAccount(any(UUID.class), any(UUID.class), any(LocalDateTime.class));
    }

    @Test
    void registerRejectsNullRequest() {
        AuthException exception = assertThrows(AuthException.class, () -> service.register(null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void registerRejectsInvalidEmailAndWeakPassword() {
        AuthModels.RegisterRequest invalidEmail = new AuthModels.RegisterRequest("Alex", "Example",
                LocalDate.of(1990, 5, 20), "not-an-email", PASSWORD, "IE", null);
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(AuthException.class, () -> service.register(invalidEmail, null, null)).getStatus());

        AuthModels.RegisterRequest weakPassword = new AuthModels.RegisterRequest("Alex", "Example",
                LocalDate.of(1990, 5, 20), "alex@example.com", "short", "IE", null);
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(AuthException.class, () -> service.register(weakPassword, null, null)).getStatus());
        verify(repository, never()).insertClient(any(), anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), any(), anyString(), any());
    }

    @Test
    void registerRejectsInvalidProfileCountryAndPhone() {
        AuthModels.RegisterRequest missingProfile = new AuthModels.RegisterRequest(" ", "Example",
                LocalDate.of(1990, 5, 20), "alex@example.com", PASSWORD, "IE", null);
        assertThrows(AuthException.class, () -> service.register(missingProfile, null, null));

        AuthModels.RegisterRequest invalidCountry = new AuthModels.RegisterRequest("Alex", "Example",
                LocalDate.of(1990, 5, 20), "alex@example.com", PASSWORD, "Ireland", null);
        assertThrows(AuthException.class, () -> service.register(invalidCountry, null, null));

        AuthModels.RegisterRequest invalidPhone = new AuthModels.RegisterRequest("Alex", "Example",
                LocalDate.of(1990, 5, 20), "alex@example.com", PASSWORD, "IE", "353123");
        assertThrows(AuthException.class, () -> service.register(invalidPhone, null, null));
    }

    @Test
    void registerMapsDuplicateEmailOrPhoneToConflict() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("bcrypt-hash");
        org.mockito.Mockito.doThrow(new DuplicateKeyException("duplicate"))
                .when(repository).insertClient(any(UUID.class), anyString(), anyString(), anyString(), anyString(),
                        any(), any(), any(), any(), anyString(), any());

        AuthException exception = assertThrows(AuthException.class,
                () -> service.register(validRegistration(), null, null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(repository, never()).insertInitialAccount(any(), any(), any());
    }

    @Test
    void loginCreatesSessionForActiveVerifiedUser() {
        AuthUser user = activeUser();
        when(repository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "bcrypt-hash")).thenReturn(true);

        AuthModels.TokenResponse response = service.login(new AuthModels.LoginRequest(" Alex@Example.com ", PASSWORD), "test-device");

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(900, response.expiresIn());
        assertFalse(response.mfaRequired());
        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(repository).insertSession(sessionCaptor.capture());
        assertEquals(CLIENT_ID, sessionCaptor.getValue().getClientId());
        assertEquals("test-device", sessionCaptor.getValue().getDevice());
        verify(repository).clearFailedLogins(CLIENT_ID);
    }

    @Test
    void loginRejectsBadPasswordAndRecordsFailure() {
        AuthUser user = activeUser();
        when(repository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "bcrypt-hash")).thenReturn(false);

        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(AuthException.class,
                () -> service.login(new AuthModels.LoginRequest("alex@example.com", "wrong"), null)).getStatus());
        verify(repository).recordFailedLogin(CLIENT_ID);
    }

    @Test
    void loginRejectsPendingVerification() {
        AuthUser user = activeUser();
        user.setAuthStatus("PENDING_VERIFICATION");
        user.setEmailVerified(false);
        when(repository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "bcrypt-hash")).thenReturn(true);

        assertEquals(HttpStatus.FORBIDDEN, assertThrows(AuthException.class,
                () -> service.login(new AuthModels.LoginRequest("alex@example.com", PASSWORD), null)).getStatus());
        verify(repository, never()).insertSession(any(AuthSession.class));
    }

    @Test
    void refreshRotatesTokens() {
        AuthSession session = activeSession();
        when(repository.findByRefreshHash(sha256(REFRESH_TOKEN))).thenReturn(Optional.of(session));
        when(repository.findById(CLIENT_ID)).thenReturn(Optional.of(activeUser()));
        when(repository.rotateSession(eq(SESSION_ID), eq(session.getRefreshTokenHash()), anyString(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        AuthModels.TokenResponse response = service.refresh(REFRESH_TOKEN);

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertFalse(response.accessToken().equals(ACCESS_TOKEN));
    }

    @Test
    void refreshRejectsInactiveSession() {
        AuthSession session = activeSession();
        session.setLastActive(LocalDateTime.now().minusMinutes(31));
        when(repository.findByRefreshHash(sha256(REFRESH_TOKEN))).thenReturn(Optional.of(session));

        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(AuthException.class,
                () -> service.refresh(REFRESH_TOKEN)).getStatus());
        verify(repository, never()).rotateSession(any(), anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void authenticateValidatesAndTouchesSession() {
        AuthSession session = activeSession();
        when(repository.findByAccessHash(sha256(ACCESS_TOKEN))).thenReturn(Optional.of(session));
        when(repository.findById(CLIENT_ID)).thenReturn(Optional.of(activeUser()));
        when(repository.touchSession(SESSION_ID)).thenReturn(1);

        assertEquals(CLIENT_ID, service.authenticate(ACCESS_TOKEN));
    }

    @Test
    void authenticateRejectsExpiredAccessToken() {
        AuthSession session = activeSession();
        session.setAccessExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(repository.findByAccessHash(sha256(ACCESS_TOKEN))).thenReturn(Optional.of(session));

        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(AuthException.class,
                () -> service.authenticate(ACCESS_TOKEN)).getStatus());
    }

    @Test
    void logoutAndLogoutAllRevokeSessions() {
        service.logout(ACCESS_TOKEN);
        service.logout(null);
        service.logoutAll(CLIENT_ID);

        verify(repository).revokeByAccessHash(sha256(ACCESS_TOKEN));
        verify(repository).revokeAllSessions(CLIENT_ID);
    }

    @Test
    void sessionsAndCurrentUserReturnClientScopedData() {
        AuthSession session = activeSession();
        when(repository.findActiveSessions(CLIENT_ID)).thenReturn(List.of(session));
        when(repository.findById(CLIENT_ID)).thenReturn(Optional.of(activeUser()));
        when(repository.findAccounts(CLIENT_ID)).thenReturn(List.of(Map.of("accountId", UUID.randomUUID())));

        assertEquals(1, service.sessions(CLIENT_ID).size());
        AuthModels.UserResponse currentUser = service.currentUser(CLIENT_ID);
        assertEquals("alex@example.com", currentUser.email());
        assertEquals(1, currentUser.accounts().size());
    }

    private AuthModels.RegisterRequest validRegistration() {
        return new AuthModels.RegisterRequest(" Alex ", " Example ", LocalDate.of(1990, 5, 20),
                " Alex@Example.com ", PASSWORD, "ie", "+353123456789");
    }

    private AuthUser activeUser() {
        AuthUser user = new AuthUser();
        user.setClientId(CLIENT_ID);
        user.setFirstName("Alex");
        user.setLastName("Example");
        user.setEmail("alex@example.com");
        user.setPasswordHash("bcrypt-hash");
        user.setPhone("+353123456789");
        user.setCountry("IE");
        user.setDateOfBirth(LocalDate.of(1990, 5, 20));
        user.setJoinDate(LocalDateTime.now());
        user.setEmailVerified(true);
        user.setAuthStatus("ACTIVE");
        return user;
    }

    private AuthSession activeSession() {
        AuthSession session = new AuthSession();
        session.setSessionId(SESSION_ID);
        session.setClientId(CLIENT_ID);
        session.setAccessTokenHash(sha256(ACCESS_TOKEN));
        session.setRefreshTokenHash(sha256(REFRESH_TOKEN));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());
        session.setAccessExpiresAt(LocalDateTime.now().plusMinutes(15));
        session.setRefreshExpiresAt(LocalDateTime.now().plusDays(30));
        return session;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
