package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;
import com.agentsbackend.repos.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    @Override
    public Admin createAdmin(Admin admin) {
        if (admin.getAdminId() == null) {
            admin.setAdminId(UUID.randomUUID());
        }
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(LocalDateTime.now());
        }
        adminRepository.createAdmin(admin);
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> findById(UUID adminId) {
        return adminRepository.findById(adminId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> findByRole(AdminRole role) {
        return adminRepository.findByRole(role);
    }
}
