package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.DTO.response.AdminAccountResponse;
import com.agentsbackend.DTO.response.AdminAuditResponse;
import com.agentsbackend.DTO.response.AdminClientResponse;
import com.agentsbackend.DTO.response.AdminInsightsResponse;
import com.agentsbackend.DTO.response.AdminOrderResponse;
import com.agentsbackend.DTO.response.TradingActivityReportResponse;
import com.agentsbackend.exceptions.AccountNotFoundException;
import com.agentsbackend.exceptions.ClientNotFoundException;
import com.agentsbackend.exceptions.OrderNotFoundException;
import com.agentsbackend.repos.transactional.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminServiceImpl Tests")
class AdminServiceImplTest {

    private AdminServiceImpl adminService;

    @Mock
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminServiceImpl(adminRepository);
    }

    // ==================== createAdmin Tests ====================

    @Test
    @DisplayName("Should create admin with provided adminId and createdAt")
    void testCreateAdminWithProvidedValues() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setCreatedAt(createdAt);

        // Act
        Admin result = adminService.createAdmin(admin);

        // Assert
        assertEquals(adminId, result.getAdminId());
        assertEquals(createdAt, result.getCreatedAt());
        verify(adminRepository).createAdmin(admin);
    }

    @Test
    @DisplayName("Should generate UUID when adminId is null")
    void testCreateAdminGeneratesUuid() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(null);
        admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        Admin result = adminService.createAdmin(admin);

        // Assert
        assertNotNull(result.getAdminId());
        verify(adminRepository).createAdmin(admin);
    }

    @Test
    @DisplayName("Should set current timestamp when createdAt is null")
    void testCreateAdminSetsCurrentTimestamp() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID());
        admin.setCreatedAt(null);
        OffsetDateTime beforeCreation = OffsetDateTime.now(ZoneOffset.UTC);

        // Act
        Admin result = adminService.createAdmin(admin);
        OffsetDateTime afterCreation = OffsetDateTime.now(ZoneOffset.UTC);

        // Assert
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(result.getCreatedAt().isBefore(afterCreation.plusSeconds(1)));
        verify(adminRepository).createAdmin(admin);
    }

    @Test
    @DisplayName("Should generate UUID and set timestamp when both are null")
    void testCreateAdminGeneratesBothUuidAndTimestamp() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(null);
        admin.setCreatedAt(null);
        OffsetDateTime beforeCreation = OffsetDateTime.now(ZoneOffset.UTC);

        // Act
        Admin result = adminService.createAdmin(admin);
        OffsetDateTime afterCreation = OffsetDateTime.now(ZoneOffset.UTC);

        // Assert
        assertNotNull(result.getAdminId());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(result.getCreatedAt().isBefore(afterCreation.plusSeconds(1)));
        verify(adminRepository).createAdmin(admin);
    }

    @Test
    @DisplayName("Should call repository createAdmin method")
    void testCreateAdminCallsRepository() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID());
        admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        adminService.createAdmin(admin);

        // Assert
        verify(adminRepository, times(1)).createAdmin(admin);
    }

    @Test
    @DisplayName("Should preserve admin fields during creation")
    void testCreateAdminPreservesFields() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        Admin result = adminService.createAdmin(admin);

        // Assert
        assertEquals(admin, result);
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void testCreateAdminHandlesRepositoryException() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID());
        admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        doThrow(new RuntimeException("Database error")).when(adminRepository).createAdmin(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.createAdmin(admin));
    }

    @Test
    @DisplayName("Should return the same admin object after creation")
    void testCreateAdminReturnsSameObject() {
        // Arrange
        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID());
        admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        Admin result = adminService.createAdmin(admin);

        // Assert
        assertSame(admin, result);
    }

    @Test
    void readMethodsDelegateToRepository() {
        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        AdminClientResponse client = new AdminClientResponse();
        AdminAccountResponse account = new AdminAccountResponse();
        AdminOrderResponse order = new AdminOrderResponse();
        TradingActivityReportResponse report = new TradingActivityReportResponse(
                1L, 1L, 0L, 0L, new BigDecimal("1"), new BigDecimal("10"));
        AdminInsightsResponse insights = new AdminInsightsResponse(
                1L, 1L, 1L, 1L, new BigDecimal("10"));

        when(adminRepository.findAllClients()).thenReturn(List.of(client));
        when(adminRepository.findClientById(clientId)).thenReturn(Optional.of(client));
        when(adminRepository.findAllAccounts()).thenReturn(List.of(account));
        when(adminRepository.findAccountById(accountId)).thenReturn(Optional.of(account));
        when(adminRepository.findAllOrders()).thenReturn(List.of(order));
        when(adminRepository.findOrderById(orderId)).thenReturn(Optional.of(order));
        when(adminRepository.findTradeById(orderId)).thenReturn(Optional.of(order));
        when(adminRepository.findAuditByOrderId(orderId)).thenReturn(List.of(new AdminAuditResponse()));
        when(adminRepository.getTradingActivityReport()).thenReturn(report);
        when(adminRepository.getInsights()).thenReturn(insights);

        assertSame(client, adminService.getClients().get(0));
        assertSame(client, adminService.getClient(clientId));
        assertSame(account, adminService.getAccounts().get(0));
        assertSame(account, adminService.getAccount(accountId));
        assertSame(order, adminService.getOrders().get(0));
        assertSame(order, adminService.getOrder(orderId));
        assertSame(order, adminService.getTrade(orderId));
        assertEquals(1, adminService.getTradeAudit(orderId).size());
        assertSame(report, adminService.getTradingActivityReport());
        assertSame(insights, adminService.getInsights());

        verify(adminRepository).findAllClients();
        verify(adminRepository).findClientById(clientId);
        verify(adminRepository).findAllAccounts();
        verify(adminRepository).findAccountById(accountId);
        verify(adminRepository).findAllOrders();
        verify(adminRepository).findOrderById(orderId);
        verify(adminRepository, times(2)).findTradeById(orderId);
        verify(adminRepository).findAuditByOrderId(orderId);
        verify(adminRepository).getTradingActivityReport();
        verify(adminRepository).getInsights();
    }

    @Test
    void missingResourcesRaiseTypedExceptions() {
        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(adminRepository.findClientById(clientId)).thenReturn(Optional.empty());
        when(adminRepository.findAccountById(accountId)).thenReturn(Optional.empty());
        when(adminRepository.findOrderById(orderId)).thenReturn(Optional.empty());
        when(adminRepository.findTradeById(orderId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> adminService.getClient(clientId));
        assertThrows(AccountNotFoundException.class, () -> adminService.getAccount(accountId));
        assertThrows(OrderNotFoundException.class, () -> adminService.getOrder(orderId));
        assertThrows(OrderNotFoundException.class, () -> adminService.getTrade(orderId));
    }
}
