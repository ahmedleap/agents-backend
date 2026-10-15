
package com.agentsbackend.services;

import com.agentsbackend.controllers.AuthModels;
import com.agentsbackend.entities.AuthUser;
import com.agentsbackend.repos.transactional.AuthRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthRepository repository;

    public AuthService(AuthRepository repository) {
        this.repository = repository;
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
