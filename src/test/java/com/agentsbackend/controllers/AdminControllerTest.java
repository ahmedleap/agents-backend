package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CreateAdminRequest;
import com.agentsbackend.DTO.response.AdminResponse;
import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;
import com.agentsbackend.services.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminController = new AdminController(adminService);
    }

    @Test
    void createAdminReturnsCreatedAdmin() {
        Admin request = new Admin();
        request.setAdminId(UUID.randomUUID());
        request.setEmail("admin@example.test");
        when(adminService.createAdmin(request)).thenReturn(request);

        ResponseEntity<Admin> response = adminController.createAdmin(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(request, response.getBody());
        verify(adminService).createAdmin(request);
    }

    @Test
    void createAdminRequestReturnsSafeResponse() {
        CreateAdminRequest request = new CreateAdminRequest(
                "First", "Last", "admin@example.test", "correct horse battery staple", AdminRole.ADMIN);
        Admin created = new Admin(UUID.randomUUID(), "First", "Last", "admin@example.test",
                "{bcrypt}hash", AdminRole.ADMIN, java.time.OffsetDateTime.now());
        when(adminService.createAdmin(request)).thenReturn(created);

        ResponseEntity<AdminResponse> response = adminController.createAdmin(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created.getAdminId(), response.getBody().adminId());
        assertEquals(created.getEmail(), response.getBody().email());
        verify(adminService).createAdmin(request);
    }
}