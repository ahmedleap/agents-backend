package com.agentsbackend.entities;

import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Client {

    private UUID clientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String passwordHash;
    private LocalDate dateOfBirth;
    private OffsetDateTime joinDate;
    private String ssnLast4;
    private PortfolioSizeRange portfolioSizeRange;
    private RiskTolerance riskTolerance;
    private String refreshToken;
    private List<Account> accounts;

    public Client() {
    }

    public Client(UUID clientId, String firstName, String middleName, String lastName, String email,
                  String passwordHash, LocalDate dateOfBirth, OffsetDateTime joinDate, String ssnLast4,
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

    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(OffsetDateTime joinDate) {
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
