package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.DTO.requests.CreateAdminRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Kept for the existing unit tests and callers that construct this service directly.
    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12);
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

    @Override
    public Admin createAdmin(CreateAdminRequest request) {
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An admin with this email already exists.");
        }

        Admin admin = new Admin();
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole(request.getRole());
        return createAdmin(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminClientResponse> getClients() {
        return adminRepository.findAllClients();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClientResponse getClient(UUID clientId) {
        return adminRepository.findClientById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAccountResponse> getAccounts() {
        return adminRepository.findAllAccounts();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountResponse getAccount(UUID accountId) {
        return adminRepository.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getOrders() {
        return adminRepository.findAllOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getOrder(UUID orderId) {
        return adminRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getTrade(UUID tradeId) {
        return adminRepository.findTradeById(tradeId)
                .orElseThrow(() -> new OrderNotFoundException("Trade not found with ID: " + tradeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAuditResponse> getTradeAudit(UUID tradeId) {
        getTrade(tradeId);
        return adminRepository.findAuditByOrderId(tradeId);
    }

    @Override
    @Transactional(readOnly = true)
    public TradingActivityReportResponse getTradingActivityReport() {
        return adminRepository.getTradingActivityReport();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminInsightsResponse getInsights() {
        return adminRepository.getInsights();
    }
}
