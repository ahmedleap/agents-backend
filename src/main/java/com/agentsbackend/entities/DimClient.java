package com.agentsbackend.entities;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DimClient represents the Client dimension from the warehouse schema.
 * Used for analytics queries against dim_clients table.
 */
public class DimClient {
    private UUID clientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private ZonedDateTime joinDate;
    private String portfolioSizeRange;
    private String riskTolerance;
    private ZonedDateTime extractTimestamp;

    // Constructors
    public DimClient() {}

    public DimClient(UUID clientId, String firstName, String lastName, String email,
                    LocalDate dateOfBirth, ZonedDateTime joinDate) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.joinDate = joinDate;
    }

    // Getters and Setters
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ZonedDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(ZonedDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public String getPortfolioSizeRange() {
        return portfolioSizeRange;
    }

    public void setPortfolioSizeRange(String portfolioSizeRange) {
        this.portfolioSizeRange = portfolioSizeRange;
    }

    public String getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(String riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public ZonedDateTime getExtractTimestamp() {
        return extractTimestamp;
    }

    public void setExtractTimestamp(ZonedDateTime extractTimestamp) {
        this.extractTimestamp = extractTimestamp;
    }
}
