package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotBlank;


public class GetHoldingRequestDTO {
    @NotBlank(message = "Account ID required")
    private String accountId;

    public GetHoldingRequestDTO(String accountId){
        this.accountId = accountId;
    }

    public String getAccountId(){ return accountId; }
    public void setAccountId( String accountId ){ this.accountId = accountId; }
}
