package com.agentsbackend.entities;

import com.agentsbackend.enums.AdminRole;
import java.time.LocalDateTime;
import java.util.UUID;

public class Admin {

    private UUID adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private AdminRole role;
    private LocalDateTime createdAt;

    // Constructors
    public Admin() {}

    public Admin(UUID adminId, String firstName, String lastName, String email, String passwordHash, AdminRole role, LocalDateTime createdAt) {
        this.adminId = adminId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    // Retrieves the unique identifier of this admin
    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }

    // Retrieves the first name of this admin
    public String getFirstName() {
        return firstName;
    }

    // Sets the first name of this admin
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Retrieves the last name of this admin
    public String getLastName() {
        return lastName;
    }

    // Sets the last name of this admin
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Retrieves the email address of this admin
    public String getEmail() {
        return email;
    }

    // Sets the email address of this admin
    public void setEmail(String email) {
        this.email = email;
    }

    // Retrieves the hashed password of this admin
    public String getPasswordHash() {
        return passwordHash;
    }

    // Sets the hashed password of this admin
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Retrieves the role (ADMIN or ANALYST) of this admin
    public AdminRole getRole() {
        return role;
    }

    // Sets the role (ADMIN or ANALYST) of this admin
    public void setRole(AdminRole role) {
        this.role = role;
    }

    // Retrieves the creation timestamp of this admin record
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Sets the creation timestamp of this admin record
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}

