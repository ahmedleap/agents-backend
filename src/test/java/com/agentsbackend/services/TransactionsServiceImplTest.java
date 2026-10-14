package com.agentsbackend.services;

import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Transaction;
import com.agentsbackend.enums.TransactionType;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionsServiceImpl business logic.
 * Tests service behavior with mocked repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TransactionsServiceImplTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountRepository accountRepository;

    private TransactionsServiceImpl transactionsService;

    private Account testAccount;
    private UUID testAccountId;
    private Transaction testTransaction1;
    private Transaction testTransaction2;
    private Transaction testTransaction3;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();

        testAccount = new Account();
        testAccount.setAccountId(testAccountId);
        testAccount.setName("Test Account");
        testAccount.setCashBalance(new BigDecimal("50000.00"));

        // Create sample transactions
        testTransaction1 = new Transaction();
        testTransaction1.setTransactionId(UUID.randomUUID());
        testTransaction1.setAccount(testAccount);
        testTransaction1.setTxnType(TransactionType.DEPOSIT);
        testTransaction1.setAmount(new BigDecimal("5000.00"));
        testTransaction1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(1));

        testTransaction2 = new Transaction();
        testTransaction2.setTransactionId(UUID.randomUUID());
        testTransaction2.setAccount(testAccount);
        testTransaction2.setTxnType(TransactionType.WITHDRAWAL);
        testTransaction2.setAmount(new BigDecimal("1000.00"));
        testTransaction2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2));

        testTransaction3 = new Transaction();
        testTransaction3.setTransactionId(UUID.randomUUID());
        testTransaction3.setAccount(testAccount);
        testTransaction3.setTxnType(TransactionType.DEPOSIT);
        testTransaction3.setAmount(new BigDecimal("2000.00"));
        testTransaction3.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(3));

        transactionsService = new TransactionsServiceImpl(transactionsRepository, accountRepository);
    }

    // ===== GET RECENT TRANSACTIONS TESTS =====
    @Test
    @DisplayName("Get recent transactions successfully")
    void testGetRecentTransactions_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 10))
                .thenReturn(List.of(testTransaction1, testTransaction2));

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTransaction1.getTransactionId(), result.get(0).getTransactionId());
        assertEquals("DEPOSIT", result.get(0).getType());
        assertEquals(new BigDecimal("5000.00"), result.get(0).getAmount());
    }

    @Test
    @DisplayName("Get recent transactions with custom limit")
    void testGetRecentTransactions_CustomLimit() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 5))
                .thenReturn(List.of(testTransaction1, testTransaction2, testTransaction3));

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 5);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(transactionsRepository, times(1)).findRecentTransactions(testAccountId, 5);
    }

    @Test
    @DisplayName("Get recent transactions with empty result")
    void testGetRecentTransactions_EmptyResult() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 10)).thenReturn(List.of());

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Get recent transactions account not found")
    void testGetRecentTransactions_AccountNotFound() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionsService.getRecentTransactions(testAccountId, 10));
    }

    @Test
    @DisplayName("Get recent transactions with single transaction")
    void testGetRecentTransactions_SingleTransaction() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 10))
                .thenReturn(List.of(testTransaction1));

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TransactionType.DEPOSIT.toString(), result.get(0).getType());
    }

    @Test
    @DisplayName("Get recent transactions verifies amount and type")
    void testGetRecentTransactions_VerifyData() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 2))
                .thenReturn(List.of(testTransaction1, testTransaction2));

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 2);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("5000.00"), result.get(0).getAmount());
        assertEquals("DEPOSIT", result.get(0).getType());
        assertEquals(new BigDecimal("1000.00"), result.get(1).getAmount());
        assertEquals("WITHDRAWAL", result.get(1).getType());
    }

    // ===== GET ALL TRANSACTIONS TESTS =====
    @Test
    @DisplayName("Get all transactions successfully")
    void testGetAllTransactions_Success() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findByAccountId(testAccountId))
                .thenReturn(List.of(testTransaction1, testTransaction2, testTransaction3));

        List<AccountsResponse.TransactionItem> result = transactionsService.getAllTransactions(testAccountId);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(transactionsRepository, times(1)).findByAccountId(testAccountId);
    }

    @Test
    @DisplayName("Get all transactions with empty result")
    void testGetAllTransactions_EmptyResult() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findByAccountId(testAccountId)).thenReturn(List.of());

        List<AccountsResponse.TransactionItem> result = transactionsService.getAllTransactions(testAccountId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Get all transactions account not found")
    void testGetAllTransactions_AccountNotFound() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionsService.getAllTransactions(testAccountId));
    }

    @Test
    @DisplayName("Get all transactions large dataset")
    void testGetAllTransactions_LargeDataset() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        // Create 100 transactions
        List<Transaction> transactions = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Transaction tx = new Transaction();
            tx.setTransactionId(UUID.randomUUID());
            tx.setAccount(testAccount);
            tx.setTxnType(i % 2 == 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);
            tx.setAmount(new BigDecimal((i + 1) * 100));
            tx.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(i));
            transactions.add(tx);
        }

        when(transactionsRepository.findByAccountId(testAccountId)).thenReturn(transactions);

        List<AccountsResponse.TransactionItem> result = transactionsService.getAllTransactions(testAccountId);

        assertNotNull(result);
        assertEquals(100, result.size());
    }

    @Test
    @DisplayName("Get all transactions maintains order")
    void testGetAllTransactions_OrderMaintained() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findByAccountId(testAccountId))
                .thenReturn(List.of(testTransaction1, testTransaction2, testTransaction3));

        List<AccountsResponse.TransactionItem> result = transactionsService.getAllTransactions(testAccountId);

        // Verify order (should be same as returned from repository)
        assertEquals(result.get(0).getTransactionId(), testTransaction1.getTransactionId());
        assertEquals(result.get(1).getTransactionId(), testTransaction2.getTransactionId());
        assertEquals(result.get(2).getTransactionId(), testTransaction3.getTransactionId());
    }

    @Test
    @DisplayName("Get recent transactions with limit of 1")
    void testGetRecentTransactions_LimitOne() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(transactionsRepository.findRecentTransactions(testAccountId, 1))
                .thenReturn(List.of(testTransaction1));

        List<AccountsResponse.TransactionItem> result = transactionsService.getRecentTransactions(testAccountId, 1);

        assertEquals(1, result.size());
        assertEquals(testTransaction1.getTransactionId(), result.get(0).getTransactionId());
    }
}
