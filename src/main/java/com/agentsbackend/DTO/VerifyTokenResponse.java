package com.agentsbackend.DTO;

import java.util.Map;

public class VerifyTokenResponse {
    private boolean valid;
    private String message;
    private Map<String, Object> claims;
    private String subject;
    private String email;
    private String role;

    public VerifyTokenResponse() {
    }

    public VerifyTokenResponse(boolean valid, String message, Map<String, Object> claims,
                               String subject, String email, String role) {
        this.valid = valid;
        this.message = message;
        this.claims = claims;
        this.subject = subject;
        this.email = email;
        this.role = role;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, Object> getClaims() { return claims; }
    public void setClaims(Map<String, Object> claims) { this.claims = claims; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
