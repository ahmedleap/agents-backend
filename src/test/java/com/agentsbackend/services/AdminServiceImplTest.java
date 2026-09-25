package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.repos.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

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
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
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
        admin.setCreatedAt(LocalDateTime.now());

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
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        Admin result = adminService.createAdmin(admin);
        LocalDateTime afterCreation = LocalDateTime.now();

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
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        Admin result = adminService.createAdmin(admin);
        LocalDateTime afterCreation = LocalDateTime.now();

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
        admin.setCreatedAt(LocalDateTime.now());

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
        admin.setCreatedAt(LocalDateTime.now());

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
        admin.setCreatedAt(LocalDateTime.now());
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
        admin.setCreatedAt(LocalDateTime.now());

        // Act
        Admin result = adminService.createAdmin(admin);

        // Assert
        assertSame(admin, result);
    }
}
