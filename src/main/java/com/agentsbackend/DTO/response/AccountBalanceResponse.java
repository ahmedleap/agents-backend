package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(
        UUID accountId,
        BigDecimal cashBalance
) {
}
