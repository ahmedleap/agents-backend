package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotBlank;


public class GetHoldingRequestDTO {
    @NotBlank(message = "Holding ID required")
    private String holdingId;

    public GetHoldingRequestDTO(String holdingId){
        this.holdingId = holdingId;
    }

    public String getHoldingId(){ return holdingId; }
    public void setHoldingId( String holdingId ){ this.holdingId = holdingId; }
}
