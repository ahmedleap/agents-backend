package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.repos.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    // Creates a new admin with auto-generated UUID and current timestamp if not provided
    @Override
    public Admin createAdmin(Admin admin) {
        if (admin.getAdminId() == null) {
            admin.setAdminId(UUID.randomUUID());
        }
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        adminRepository.createAdmin(admin);
        return admin;
    }
}
