package com.agentsbackend.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingActivityReportResponse {
    private Long totalOrders;
    private Long filledOrders;
    private Long pendingOrders;
    private Long cancelledOrders;
    private BigDecimal filledQuantity;
    private BigDecimal tradedValue;
}