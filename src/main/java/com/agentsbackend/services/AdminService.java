package com.agentsbackend.services;

import com.agentsbackend.entities.Admin;

public interface AdminService {
    // Creates and persists a new admin record
    Admin createAdmin(Admin admin);
}
