package com.agentsbackend.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminInsightsResponse {
    private Long clientCount;
    private Long accountCount;
    private Long orderCount;
    private Long filledOrderCount;
    private BigDecimal tradedValue;
}