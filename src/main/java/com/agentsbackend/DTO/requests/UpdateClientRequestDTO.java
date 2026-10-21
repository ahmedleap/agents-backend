package com.agentsbackend.DTO.requests;

import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a client's profile from the settings page (PATCH /api/client/v1/{clientId}).
 * Every field is optional: null (or left out of the JSON) means "don't change it".
 * middleName can be cleared by sending an empty string "".
 * firstName and lastName are required columns, so they can be changed but never cleared.
 */
public class UpdateClientRequestDTO {

    @Size(max = 50, message = "First name must be 50 characters or fewer")
    @Pattern(regexp = ".*\\S.*", message = "First name cannot be blank")
    private String firstName;

    @Size(max = 50, message = "Middle name must be 50 characters or fewer")
    private String middleName;

    @Size(max = 50, message = "Last name must be 50 characters or fewer")
    @Pattern(regexp = ".*\\S.*", message = "Last name cannot be blank")
    private String lastName;

    private RiskTolerance riskTolerance;

    private PortfolioSizeRange portfolioSizeRange;

    // Constructors
    public UpdateClientRequestDTO() {}

    public UpdateClientRequestDTO(String firstName, String middleName, String lastName,
                                  RiskTolerance riskTolerance, PortfolioSizeRange portfolioSizeRange) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.riskTolerance = riskTolerance;
        this.portfolioSizeRange = portfolioSizeRange;
    }

    /** True if at least one field was sent, i.e. there is something to update. */
    public boolean hasAnyField() {
        return firstName != null || middleName != null || lastName != null || riskTolerance != null || portfolioSizeRange != null;
    }

    // Getters and Setters
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

    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public PortfolioSizeRange getPortfolioSizeRange() {
        return portfolioSizeRange;
    }

    public void setPortfolioSizeRange(PortfolioSizeRange portfolioSizeRange) {
        this.portfolioSizeRange = portfolioSizeRange;
    }

    @Override
    public String toString() {
        return "UpdateClientRequestDTO{" +
                "firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", riskTolerance=" + riskTolerance +
                ", portfolioSizeRange=" + portfolioSizeRange +
                '}';
    }
}
