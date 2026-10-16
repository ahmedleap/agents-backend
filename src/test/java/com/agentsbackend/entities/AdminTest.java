package com.agentsbackend.entities;

import com.agentsbackend.enums.AdminRole;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminTest {

    @Test
    void constructorAndAccessorsRetainAdminValues() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        Admin admin = new Admin(id, "First", "Last", "admin@example.test", "hash", AdminRole.ADMIN, createdAt);

        assertEquals(id, admin.getAdminId());
        assertEquals("First", admin.getFirstName());
        assertEquals("Last", admin.getLastName());
        assertEquals("admin@example.test", admin.getEmail());
        assertEquals("hash", admin.getPasswordHash());
        assertEquals(AdminRole.ADMIN, admin.getRole());
        assertEquals(createdAt, admin.getCreatedAt());
        assertEquals("Admin{adminId=" + id + ", firstName='First', lastName='Last', " +
                "email='admin@example.test', role=ADMIN, createdAt=" + createdAt + "}", admin.toString());
    }

    @Test
    void settersUpdateAdminValues() {
        Admin admin = new Admin();
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-01T00:00:00Z");

        admin.setAdminId(id);
        admin.setFirstName("UpdatedFirst");
        admin.setLastName("UpdatedLast");
        admin.setEmail("updated@example.test");
        admin.setPasswordHash("updated-hash");
        admin.setRole(AdminRole.ANALYST);
        admin.setCreatedAt(createdAt);

        assertEquals(id, admin.getAdminId());
        assertEquals("UpdatedFirst", admin.getFirstName());
        assertEquals("UpdatedLast", admin.getLastName());
        assertEquals("updated@example.test", admin.getEmail());
        assertEquals("updated-hash", admin.getPasswordHash());
        assertEquals(AdminRole.ANALYST, admin.getRole());
        assertEquals(createdAt, admin.getCreatedAt());
    }
}