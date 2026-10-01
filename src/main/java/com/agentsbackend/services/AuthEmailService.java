package com.agentsbackend.services;

public interface AuthEmailService {
    void sendEmailVerification(String email, String token);
    void sendPasswordReset(String email, String token);
    void sendPasswordChanged(String email);
}
