package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminClientResponse {
    private UUID clientId;
    private String clientName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private OffsetDateTime joinDate;
    private String ssnLast4;
    private PortfolioSizeRange portfolioSizeRange;
    private RiskTolerance riskTolerance;
    /** Cash plus holdings at current prices, across the client's active accounts */
    private BigDecimal portfolioValue;
}