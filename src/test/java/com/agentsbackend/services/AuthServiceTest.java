
package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.AuthRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
