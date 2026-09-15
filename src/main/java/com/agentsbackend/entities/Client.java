package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;
}
