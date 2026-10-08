package com.agentsbackend.services;

import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MissionAuthorizationServiceTest {
    private static final UUID CLIENT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID OTHER_CLIENT_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID ORDER_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final MissionAuthorizationService authorizationService =
            new MissionAuthorizationService(accountRepository, orderRepository);

    @Test
    void permitsClientWhenAccountBelongsToJwtSubject() {
        when(accountRepository.existsByIdAndClientId(ACCOUNT_ID, CLIENT_ID)).thenReturn(true);

        authorizationService.requireOwnAccount(CLIENT_ID, ACCOUNT_ID);

        verify(accountRepository).existsByIdAndClientId(ACCOUNT_ID, CLIENT_ID);
    }

    @Test
    void rejectsAccountOwnedByAnotherClientWithoutRevealingItsExistence() {
        when(accountRepository.existsByIdAndClientId(ACCOUNT_ID, CLIENT_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorizationService.requireOwnAccount(CLIENT_ID, ACCOUNT_ID));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void rejectsClientIdSpoofing() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorizationService.requireOwnClient(CLIENT_ID, OTHER_CLIENT_ID));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void checksOrderThroughItsOwningAccount() {
        when(orderRepository.findAccountIdByOrderId(ORDER_ID)).thenReturn(ACCOUNT_ID);
        when(accountRepository.existsByIdAndClientId(ACCOUNT_ID, CLIENT_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorizationService.requireOwnOrder(CLIENT_ID, ORDER_ID));

        assertEquals(404, exception.getStatusCode().value());
        verify(orderRepository).findAccountIdByOrderId(ORDER_ID);
    }
}
