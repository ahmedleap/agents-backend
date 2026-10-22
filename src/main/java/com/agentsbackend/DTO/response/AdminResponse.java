package com.agentsbackend.DTO.response;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminResponse(
        UUID adminId,
        String firstName,
        String lastName,
        String email,
        AdminRole role,
        OffsetDateTime createdAt) {

    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getAdminId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getCreatedAt());
    }
}
