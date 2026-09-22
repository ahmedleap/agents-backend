package com.agentsbackend.DTO.response;

import java.util.UUID;

public class CreateHoldingResponseDTO {
    private UUID holdingId;

    //constructor
    public CreateHoldingResponseDTO(UUID holdingId){
        this.holdingId = holdingId;
    }

    //Getters and Setters
    public UUID getHoldingId() {return holdingId;}
    public void setHoldingId(UUID holdingId){this.holdingId = holdingId;}
}
