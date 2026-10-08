package com.agentsbackend.services;

import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Resource-level authorization based on the client UUID from Spring's verified JWT principal. */
@Service
public class MissionAuthorizationService {
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public MissionAuthorizationService(AccountRepository accountRepository, OrderRepository orderRepository) {
        this.accountRepository = accountRepository;
        this.orderRepository = orderRepository;
    }

    public void requireOwnClient(UUID authenticatedClientId, UUID requestedClientId) {
        if (authenticatedClientId == null || !authenticatedClientId.equals(requestedClientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not authorized to access this client's resources.");
        }
    }

    public void requireOwnAccount(UUID authenticatedClientId, UUID accountId) {
        if (authenticatedClientId == null || accountId == null
                || !accountRepository.existsByIdAndClientId(accountId, authenticatedClientId)) {
            // 404 avoids disclosing whether another client's account ID exists.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        }
    }

    public void requireOwnOrder(UUID authenticatedClientId, UUID orderId) {
        UUID accountId = orderRepository.findAccountIdByOrderId(orderId);
        if (accountId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.");
        }
        requireOwnAccount(authenticatedClientId, accountId);
    }

    public void requireOwnAccountOrPrivilegedRead(Jwt jwt, UUID accountId) {
        if (hasPrivilegedReadRole(jwt)) return;
        requireOwnAccount(clientId(jwt), accountId);
    }

    public void requireOwnOrderOrPrivilegedRead(Jwt jwt, UUID orderId) {
        if (hasPrivilegedReadRole(jwt)) return;
        requireOwnOrder(clientId(jwt), orderId);
    }

    public UUID clientId(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated subject.");
        }
    }

    private boolean hasPrivilegedReadRole(Jwt jwt) {
        if (jwt == null) return false;
        Object roleClaim = jwt.getClaims().get("role");
        if (roleClaim instanceof String role && isPrivileged(role)) return true;
        Object rolesClaim = jwt.getClaims().get("roles");
        return rolesClaim instanceof Iterable<?> roles && java.util.stream.StreamSupport.stream(roles.spliterator(), false)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(this::isPrivileged);
    }

    private boolean isPrivileged(String role) {
        String normalized = role.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN")
                || normalized.equals("ANALYST") || normalized.equals("ROLE_ANALYST");
    }
}
