package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.repos.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        adminRepository.createAdmin(admin);
        return admin;
    }
}
