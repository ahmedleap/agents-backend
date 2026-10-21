
package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.transactional.AuthRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agentsbackend.entities.AuthSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final UUID CLIENT_ID =
        UUID.fromString("beefca11-0000-4000-8000-000000000001");

    @Mock
    private AuthRepository repository;

    private AuthService service;

    @BeforeEach
    void setUp() {
        // AuthService no longer requires PasswordEncoder.
        service = new AuthService(repository);
    }

    @Test
    void isSessionValidAcceptsMatchingSubjectForAnyIdentityRole() {
        UUID subjectId = UUID.fromString("b0000000-0000-4000-8000-000000000001");
        UUID sessionId = UUID.fromString("b0000000-0000-4000-8000-000000000002");
        String accessToken = "access-token-value";

        AuthSession session = new AuthSession();
        session.setSessionId(sessionId);
        session.setClientId(subjectId);
        session.setAccessExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10));
        session.setLastActive(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));

        when(repository.findByAccessHash(anyString())).thenReturn(Optional.of(session));
        when(repository.touchSession(sessionId)).thenReturn(1);

        boolean valid = service.isSessionValid(accessToken, subjectId);

        assertTrue(valid);
        verify(repository).findByAccessHash(anyString());
        verify(repository).touchSession(sessionId);
    }

    @Test
    void isSessionValidRejectsMismatchedSubject() {
        UUID subjectId = UUID.fromString("c0000000-0000-4000-8000-000000000001");
        UUID differentSessionOwner = UUID.fromString("c0000000-0000-4000-8000-000000000002");
        String accessToken = "access-token-value";

        AuthSession session = new AuthSession();
        session.setSessionId(UUID.fromString("c0000000-0000-4000-8000-000000000003"));
        session.setClientId(differentSessionOwner);
        session.setAccessExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10));
        session.setLastActive(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));

        when(repository.findByAccessHash(anyString())).thenReturn(Optional.of(session));

        boolean valid = service.isSessionValid(accessToken, subjectId);

        assertFalse(valid);
        verify(repository).findByAccessHash(anyString());
        verify(repository, never()).touchSession(any(UUID.class));
    }

    @Test
    void currentUserReturnsClientProfileAndAccounts() {

        AuthUser user = createActiveUser();

        when(repository.findById(CLIENT_ID))
            .thenReturn(Optional.of(user));

        when(repository.findAccounts(CLIENT_ID))
            .thenReturn(List.of(
                Map.of("accountId", UUID.randomUUID())
            ));

        AuthModels.UserResponse response =
            service.currentUser(CLIENT_ID);

        assertNotNull(response);
        assertEquals(CLIENT_ID, response.clientId());
        assertEquals("alex@example.com", response.email());
        assertEquals(1, response.accounts().size());

        verify(repository).findById(CLIENT_ID);
        verify(repository).findAccounts(CLIENT_ID);
    }

    @Test
    void currentUserRejectsUnknownClient() {

        when(repository.findById(CLIENT_ID))
            .thenReturn(Optional.empty());

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.currentUser(CLIENT_ID)
            );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verify(repository, never())
            .findAccounts(CLIENT_ID);
    }

    @Test
    void currentUserRejectsInactiveClient() {

        AuthUser user = createActiveUser();
        user.setAuthStatus("LOCKED");

        when(repository.findById(CLIENT_ID))
            .thenReturn(Optional.of(user));

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.currentUser(CLIENT_ID)
            );

        assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
        );

        verify(repository, never())
            .findAccounts(CLIENT_ID);
    }

    @Test
    void currentUserRejectsNullClientId() {

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.currentUser(null)
            );

        assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
        );

        verifyNoInteractions(repository);
    }

    private AuthUser createActiveUser() {

        AuthUser user = new AuthUser();

        user.setClientId(CLIENT_ID);
        user.setFirstName("Alex");
        user.setLastName("Example");
        user.setEmail("alex@example.com");
        user.setPhone("+353123456789");
        user.setCountry("IE");
        user.setDateOfBirth(LocalDate.of(1990, 5, 20));
        user.setJoinDate(LocalDateTime.now());
        user.setEmailVerified(true);
        user.setAuthStatus("ACTIVE");

        return user;
    }
}
