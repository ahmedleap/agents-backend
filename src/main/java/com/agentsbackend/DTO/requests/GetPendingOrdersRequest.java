package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.UUID;

/**
 * Request DTO for retrieving pending orders with optional filtering and pagination.
 */
public class GetPendingOrdersRequest {

    private UUID accountId;

    @Min(value = 0, message = "Limit must be at least 0")
    @Max(value = 1000, message = "Limit cannot exceed 1000")
    private Integer limit;

    @Min(value = 0, message = "Offset must be at least 0")
    private Integer offset;

    public GetPendingOrdersRequest() {
        this.limit = 50;
        this.offset = 0;
    }

    public GetPendingOrdersRequest(UUID accountId, Integer limit, Integer offset) {
        this.accountId = accountId;
        this.limit = limit != null ? limit : 50;
        this.offset = offset != null ? offset : 0;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit != null ? limit : 50;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset != null ? offset : 0;
    }
}
