package com.agentsbackend.controllers;

import com.agentsbackend.entities.Admin;
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
}