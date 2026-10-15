package com.agentsbackend.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditResponse {
    private UUID auditLogId;
    private UUID orderId;
    private UUID accountId;
    private UUID clientId;
    private String eventType;
    private OffsetDateTime eventTime;
    private String reason;
    private String details;
    private OffsetDateTime createdAt;
}