package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;

public class ClientResponseDTO {
    private String clientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private RiskTolerance riskTolerance;
    private PortfolioSizeRange portfolioSizeRange;

    // No-arg constructor
    public ClientResponseDTO() {
    }

    //constructor - data container only, no calculations
    public ClientResponseDTO(String clientId, String firstName, String middleName,
                                   String lastName, String email, RiskTolerance riskTolerance,
                                   PortfolioSizeRange portfolioSizeRange){
        this.clientId = clientId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.riskTolerance = riskTolerance;
        this.portfolioSizeRange = portfolioSizeRange;
    }

    //Getters and Setters
    public String getClientId() {return clientId;}
    public void setClientId(String clientId){this.clientId = clientId;}

    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName){this.firstName = firstName;}

    public String getMiddleName() {return middleName;}
    public void setMiddleName(String middleName){this.middleName = middleName;}

    public String getLastName() {return lastName;}
    public void setLastName(String lastName){this.lastName = lastName;}

    public String getEmail() {return email;}
    public void setEmail(String email){this.email = email;}

    public RiskTolerance getRiskTolerance() {return riskTolerance;}
    public void setRiskTolerance(RiskTolerance riskTolerance){this.riskTolerance = riskTolerance;}

    public PortfolioSizeRange getPortfolioSizeRange() {return portfolioSizeRange;}
    public void setPortfolioSizeRange(PortfolioSizeRange portfolioSizeRange){this.portfolioSizeRange = portfolioSizeRange;}
}
