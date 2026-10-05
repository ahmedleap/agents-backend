package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.services.AccountService;
import com.agentsbackend.services.TransactionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountController REST endpoints.
 * Tests controller behavior with mocked service dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionsService transactionsService;

    private AccountController accountController;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        accountController = new AccountController(accountService, transactionsService);
    }

    // ===== GET RECENT TRANSACTIONS ENDPOINT TESTS =====
    @Test
    @DisplayName("Get recent transactions successfully")
    void testGetRecentTransactions_Success() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "WITHDRAWAL",
                        new BigDecimal("1000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("DEPOSIT", response.getBody().get(0).getType());
        assertEquals("WITHDRAWAL", response.getBody().get(1).getType());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with default limit")
    void testGetRecentTransactions_DefaultLimit() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with custom limit")
    void testGetRecentTransactions_CustomLimit() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("2000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)
                ),
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "WITHDRAWAL",
                        new BigDecimal("1000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(2)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 5))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 5);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 5);
    }

    @Test
    @DisplayName("Get recent transactions with empty list")
    void testGetRecentTransactions_EmptyList() {
        // Arrange
        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions account not found")
    void testGetRecentTransactions_AccountNotFound() {
        // Arrange
        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenThrow(new IllegalArgumentException("Account not found: " + testAccountId));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> accountController.getRecentTransactions(testAccountId, 10));
        verify(transactionsService, times(1)).getRecentTransactions(testAccountId, 10);
    }

    @Test
    @DisplayName("Get recent transactions with limit of 1")
    void testGetRecentTransactions_LimitOne() {
        // Arrange
        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(
                        UUID.randomUUID(),
                        "DEPOSIT",
                        new BigDecimal("5000.00"),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
        );

        when(transactionsService.getRecentTransactions(testAccountId, 1))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions with large limit")
    void testGetRecentTransactions_LargeLimit() {
        // Arrange - Create 50 transactions
        List<AccountsResponse.TransactionItem> mockTransactions = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mockTransactions.add(new AccountsResponse.TransactionItem(
                    UUID.randomUUID(),
                    i % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL",
                    new BigDecimal((i + 1) * 100),
                    OffsetDateTime.now(ZoneOffset.UTC).minusHours(i)
            ));
        }

        when(transactionsService.getRecentTransactions(testAccountId, 100))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 100);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50, response.getBody().size());
    }

    @Test
    @DisplayName("Get recent transactions verifies transaction data")
    void testGetRecentTransactions_VerifyData() {
        // Arrange
        UUID txnId1 = UUID.randomUUID();
        UUID txnId2 = UUID.randomUUID();
        BigDecimal amount1 = new BigDecimal("5000.00");
        BigDecimal amount2 = new BigDecimal("1000.00");
        OffsetDateTime time1 = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime time2 = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);

        List<AccountsResponse.TransactionItem> mockTransactions = List.of(
                new AccountsResponse.TransactionItem(txnId1, "DEPOSIT", amount1, time1),
                new AccountsResponse.TransactionItem(txnId2, "WITHDRAWAL", amount2, time2)
        );

        when(transactionsService.getRecentTransactions(testAccountId, 10))
                .thenReturn(mockTransactions);

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertEquals(txnId1, response.getBody().get(0).getTransactionId());
        assertEquals("DEPOSIT", response.getBody().get(0).getType());
        assertEquals(amount1, response.getBody().get(0).getAmount());
        assertEquals(time1, response.getBody().get(0).getCreatedAt());

        assertEquals(txnId2, response.getBody().get(1).getTransactionId());
        assertEquals("WITHDRAWAL", response.getBody().get(1).getType());
        assertEquals(amount2, response.getBody().get(1).getAmount());
        assertEquals(time2, response.getBody().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("Get recent transactions response type is OK")
    void testGetRecentTransactions_ResponseType() {
        // Arrange
        when(transactionsService.getRecentTransactions(any(UUID.class), anyInt()))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<AccountsResponse.TransactionItem>> response =
                accountController.getRecentTransactions(testAccountId, 10);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
