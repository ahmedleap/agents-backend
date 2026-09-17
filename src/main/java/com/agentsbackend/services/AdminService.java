package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;

import com.agentsbackend.enums.AdminRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminService {
    Admin createAdmin(Admin admin);

    List<Admin> findAll();

    Optional<Admin> findById(UUID adminId);

    Optional<Admin> findByEmail(String email);

    List<Admin> findByRole(AdminRole role);
}
