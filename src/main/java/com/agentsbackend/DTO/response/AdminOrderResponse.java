package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {
    private UUID orderId;
    private UUID accountId;
    private UUID clientId;
    private String clientName;
    private UUID instrumentId;
    private String ticker;
    private String instrumentName;
    private OrderType orderType;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private BigDecimal filledPrice;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime filledAt;
    private OffsetDateTime cancelledAt;
    private String cancelReason;
}