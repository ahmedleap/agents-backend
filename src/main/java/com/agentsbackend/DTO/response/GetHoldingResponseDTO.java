package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

public class GetHoldingResponseDTO {
    private String holdingId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal averageCostBasis;

    //constructor
    public GetHoldingResponseDTO(String holdingId,String accountId, String instrumentId, BigDecimal quantity, BigDecimal averageCostBasis){
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
    }

    //Getters and Setters
    public String getHoldingId() {return holdingId;}
    public void setHoldingId(String holdingId){this.holdingId = holdingId;}

    public String getAccountId() {return accountId;}
    public void setAccountId(String accountId){this.accountId = accountId;}

    public String getInstrumentId() {return instrumentId;}
    public void setInstrumentId(String instrumentId){this.instrumentId = instrumentId;}

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity( BigDecimal quantity ) { this.quantity = quantity; }

    public BigDecimal getAverageCostBasis() { return averageCostBasis; }
    public void setAverageCostBasis( BigDecimal averageCostBasis ) { this.averageCostBasis = averageCostBasis; }
}
