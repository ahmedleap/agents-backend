package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Account entity validation and property mutations.
 * Tests entity behavior without database dependencies.
 */
class AccountRepositoryTest {

    private Account testAccount;
    private Client testClient;
    private UUID testAccountId;
    private UUID testClientId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testClientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setClientId(testClientId);

        testAccount = new Account();
        testAccount.setAccountId(testAccountId);
        testAccount.setClient(testClient);
        testAccount.setName("Test Account");
        testAccount.setCashBalance(new BigDecimal("50000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setOpenDate(LocalDateTime.now());
    }

    // ===== Account Creation Tests =====
    @Test
    @DisplayName("Account creation with all properties")
    void testAccountCreation() {
        assertNotNull(testAccount.getAccountId());
        assertEquals(testAccountId, testAccount.getAccountId());
        assertEquals("Test Account", testAccount.getName());
        assertEquals(new BigDecimal("50000.00"), testAccount.getCashBalance());
        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
        assertNotNull(testAccount.getOpenDate());
    }

    @Test
    @DisplayName("Account creation with minimum properties")
    void testAccountCreationMinimal() {
        Account minimal = new Account();
        assertNull(minimal.getAccountId());
        assertNull(minimal.getName());
        assertNull(minimal.getCashBalance());
    }

    @Test
    @DisplayName("Account creation with zero balance")
    void testAccountCreationZeroBalance() {
        testAccount.setCashBalance(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, testAccount.getCashBalance());
    }

    @Test
    @DisplayName("Account creation with large balance")
    void testAccountCreationLargeBalance() {
        BigDecimal largeBalance = new BigDecimal("999999999.99");
        testAccount.setCashBalance(largeBalance);
        assertEquals(largeBalance, testAccount.getCashBalance());
    }

    // ===== Account ID Tests =====
    @Test
    @DisplayName("Account ID is properly set")
    void testAccountIdSet() {
        assertEquals(testAccountId, testAccount.getAccountId());
    }

    @Test
    @DisplayName("Account ID can be changed")
    void testAccountIdChange() {
        UUID newId = UUID.randomUUID();
        testAccount.setAccountId(newId);
        assertEquals(newId, testAccount.getAccountId());
        assertNotEquals(testAccountId, testAccount.getAccountId());
    }

    @Test
    @DisplayName("Account ID is unique")
    void testAccountIdUnique() {
        UUID id2 = UUID.randomUUID();
        assertNotEquals(testAccountId, id2);
    }

    // ===== Client Tests =====
    @Test
    @DisplayName("Account has associated client")
    void testAccountHasClient() {
        assertNotNull(testAccount.getClient());
        assertEquals(testClientId, testAccount.getClient().getClientId());
    }

    @Test
    @DisplayName("Account client can be changed")
    void testAccountClientChange() {
        UUID newClientId = UUID.randomUUID();
        Client newClient = new Client();
        newClient.setClientId(newClientId);
        testAccount.setClient(newClient);
        assertEquals(newClientId, testAccount.getClient().getClientId());
    }

    // ===== Name Tests =====
    @Test
    @DisplayName("Account name can be updated")
    void testAccountNameUpdate() {
        testAccount.setName("Updated Account");
        assertEquals("Updated Account", testAccount.getName());
    }

    @Test
    @DisplayName("Account name with special characters")
    void testAccountNameSpecialCharacters() {
        testAccount.setName("Test Account @#$%");
        assertEquals("Test Account @#$%", testAccount.getName());
    }

    @Test
    @DisplayName("Account name can be null")
    void testAccountNameNull() {
        testAccount.setName(null);
        assertNull(testAccount.getName());
    }

    @Test
    @DisplayName("Account name with unicode characters")
    void testAccountNameUnicode() {
        testAccount.setName("账户 🔐");
        assertEquals("账户 🔐", testAccount.getName());
    }

    @Test
    @DisplayName("Account name with very long string")
    void testAccountNameLongString() {
        String longName = "A".repeat(255);
        testAccount.setName(longName);
        assertEquals(longName, testAccount.getName());
    }

    // ===== Cash Balance Tests =====
    @Test
    @DisplayName("Cash balance precision with BigDecimal")
    void testCashBalancePrecision() {
        BigDecimal precise = new BigDecimal("12345.67");
        testAccount.setCashBalance(precise);
        assertEquals(precise, testAccount.getCashBalance());
        assertEquals("12345.67", testAccount.getCashBalance().toPlainString());
    }

    @Test
    @DisplayName("Cash balance can be negative")
    void testCashBalanceNegative() {
        BigDecimal negative = new BigDecimal("-5000.00");
        testAccount.setCashBalance(negative);
        assertTrue(testAccount.getCashBalance().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Cash balance very small positive")
    void testCashBalanceVerySmall() {
        BigDecimal small = new BigDecimal("0.01");
        testAccount.setCashBalance(small);
        assertEquals(small, testAccount.getCashBalance());
    }

    @Test
    @DisplayName("Cash balance can be null")
    void testCashBalanceNull() {
        testAccount.setCashBalance(null);
        assertNull(testAccount.getCashBalance());
    }

    @Test
    @DisplayName("Cash balance extreme value")
    void testCashBalanceExtreme() {
        BigDecimal extreme = new BigDecimal("9999999999.99");
        testAccount.setCashBalance(extreme);
        assertEquals(extreme, testAccount.getCashBalance());
    }

    @Test
    @DisplayName("Cash balance zero")
    void testCashBalanceZero() {
        testAccount.setCashBalance(BigDecimal.ZERO);
        assertEquals(0, testAccount.getCashBalance().compareTo(BigDecimal.ZERO));
    }

    // ===== Account Status Tests =====
    @Test
    @DisplayName("Account status is ACTIVE")
    void testAccountStatusActive() {
        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
    }

    @Test
    @DisplayName("Account status can be changed to CLOSED")
    void testAccountStatusClosed() {
        testAccount.setStatus(AccountStatus.CLOSED);
        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
    }

    @Test
    @DisplayName("Account status transition from ACTIVE to CLOSED")
    void testAccountStatusTransition() {
        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
        testAccount.setStatus(AccountStatus.CLOSED);
        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
    }

    @Test
    @DisplayName("Account status can be null")
    void testAccountStatusNull() {
        testAccount.setStatus(null);
        assertNull(testAccount.getStatus());
    }

    @Test
    @DisplayName("Account status reversal")
    void testAccountStatusReversal() {
        testAccount.setStatus(AccountStatus.CLOSED);
        testAccount.setStatus(AccountStatus.ACTIVE);
        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
    }

    @Test
    @DisplayName("Account status transition CLOSED to ACTIVE")
    void testAccountStatusReopenTransition() {
        testAccount.setStatus(AccountStatus.CLOSED);
        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
        testAccount.setStatus(AccountStatus.ACTIVE);
        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
    }

    // ===== Open Date Tests =====
    @Test
    @DisplayName("Account open date is set")
    void testAccountOpenDateSet() {
        assertNotNull(testAccount.getOpenDate());
    }

    @Test
    @DisplayName("Account open date can be updated")
    void testAccountOpenDateUpdate() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        testAccount.setOpenDate(newDate);
        assertEquals(newDate, testAccount.getOpenDate());
    }

    @Test
    @DisplayName("Account open date can be null")
    void testAccountOpenDateNull() {
        testAccount.setOpenDate(null);
        assertNull(testAccount.getOpenDate());
    }

    // ===== Full Account State Tests =====
    @Test
    @DisplayName("All properties stored correctly together")
    void testFullAccountState() {
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setClientId(clientId);
        String name = "Full State Account";
        BigDecimal balance = new BigDecimal("100000.00");
        AccountStatus status = AccountStatus.ACTIVE;
        LocalDateTime date = LocalDateTime.now();

        Account account = new Account();
        account.setAccountId(accountId);
        account.setClient(client);
        account.setName(name);
        account.setCashBalance(balance);
        account.setStatus(status);
        account.setOpenDate(date);

        assertEquals(accountId, account.getAccountId());
        assertEquals(clientId, account.getClient().getClientId());
        assertEquals(name, account.getName());
        assertEquals(balance, account.getCashBalance());
        assertEquals(status, account.getStatus());
        assertEquals(date, account.getOpenDate());
    }

    @Test
    @DisplayName("Multiple property updates")
    void testMultiplePropertyUpdates() {
        testAccount.setName("Updated Name");
        testAccount.setCashBalance(new BigDecimal("75000.00"));
        testAccount.setStatus(AccountStatus.CLOSED);

        assertEquals("Updated Name", testAccount.getName());
        assertEquals(new BigDecimal("75000.00"), testAccount.getCashBalance());
        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
    }

    @Test
    @DisplayName("Account properties are independent")
    void testAccountPropertyIndependence() {
        Account account2 = new Account();
        account2.setAccountId(UUID.randomUUID());
        account2.setName("Account 2");

        assertNotEquals(testAccount.getAccountId(), account2.getAccountId());
        assertNotEquals(testAccount.getName(), account2.getName());
    }

    // ===== Entity Independence Tests =====
    @Test
    @DisplayName("Multiple accounts don't interfere with each other")
    void testAccountIndependence() {
        Account account2 = new Account();
        account2.setAccountId(UUID.randomUUID());
        account2.setName("Second Account");
        account2.setCashBalance(new BigDecimal("25000.00"));

        assertEquals("Test Account", testAccount.getName());
        assertEquals("Second Account", account2.getName());
        assertNotEquals(testAccount.getAccountId(), account2.getAccountId());
    }

    @Test
    @DisplayName("Modifying one account doesn't affect another")
    void testAccountMutabilityIndependence() {
        Account account2 = new Account();
        account2.setAccountId(UUID.randomUUID());
        account2.setCashBalance(new BigDecimal("10000.00"));

        testAccount.setName("Modified");
        testAccount.setCashBalance(new BigDecimal("60000.00"));

        assertNotEquals("Modified", account2.getName());
        assertNotEquals(new BigDecimal("60000.00"), account2.getCashBalance());
    }

    @Test
    @DisplayName("Account copy creates independent instances")
    void testAccountCopyIndependence() {
        Account accountCopy = new Account();
        accountCopy.setAccountId(testAccount.getAccountId());
        accountCopy.setName(testAccount.getName());
        accountCopy.setCashBalance(testAccount.getCashBalance());

        accountCopy.setName("Changed");
        accountCopy.setCashBalance(new BigDecimal("99999.00"));

        assertEquals("Test Account", testAccount.getName());
        assertEquals(new BigDecimal("50000.00"), testAccount.getCashBalance());
    }
}
