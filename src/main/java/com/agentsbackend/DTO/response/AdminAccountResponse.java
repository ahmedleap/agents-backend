package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountResponse {
    private UUID accountId;
    private UUID clientId;
    private String clientName;
    private String name;
    private BigDecimal cashBalance;
    private AccountStatus status;
    private OffsetDateTime openDate;
}