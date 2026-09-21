package com.agentsbackend.entities;

import jakarta.persistence.*;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @Column(name = "client_id", columnDefinition = "UUID")
    private UUID clientId;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "join_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime joinDate;

    @Column(name = "ssn_last4", length = 4)
    private String ssnLast4;

    @Column(name = "portfolio_size_range")
    @Enumerated(EnumType.STRING)
    private PortfolioSizeRange portfolioSizeRange;

    @Column(name = "risk_tolerance")
    @Enumerated(EnumType.STRING)
    private RiskTolerance riskTolerance;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    public Client() {
    }

    public Client(UUID clientId, String firstName, String middleName, String lastName, String email,
                  String passwordHash, LocalDate dateOfBirth, LocalDateTime joinDate, String ssnLast4,
                  PortfolioSizeRange portfolioSizeRange, RiskTolerance riskTolerance, String refreshToken, List<Account> accounts) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.dateOfBirth = dateOfBirth;
        this.joinDate = joinDate;
        this.ssnLast4 = ssnLast4;
        this.portfolioSizeRange = portfolioSizeRange;
        this.riskTolerance = riskTolerance;
        this.refreshToken = refreshToken;
        this.accounts = accounts;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public String getSsnLast4() {
        return ssnLast4;
    }

    public void setSsnLast4(String ssnLast4) {
        this.ssnLast4 = ssnLast4;
    }

    public PortfolioSizeRange getPortfolioSizeRange() {
        return portfolioSizeRange;
    }

    public void setPortfolioSizeRange(PortfolioSizeRange portfolioSizeRange) {
        this.portfolioSizeRange = portfolioSizeRange;
    }

    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    // Retrieves the OAuth/API refresh token for this client
    public String getRefreshToken() {
        return refreshToken;
    }

    // Sets the OAuth/API refresh token for this client
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
