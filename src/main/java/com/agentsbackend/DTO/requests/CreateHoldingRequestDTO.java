package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

public class CreateHoldingRequestDTO{
    @NotBlank(message = "Account requires ID")
    private String accountId;

    @NotBlank (message = "Instrument requires Id")
    private String instrumentId;

    @NotBlank (message = "Holding requires quantity")
    @Positive (message = "Quantity must be greater than 0")
    @Max(value = 1000000, message = "Quantity cannot exceed 1,000,000 shares")
    private BigDecimal quantity;

    @NotBlank (message = "Holding requires average cost basis")
    @Positive (message = "Avergae cost must be greater than 0")
    private BigDecimal averageCostBasis;

    //constructor
    public CreateHoldingRequestDTO(String accountId, String instrumentId, BigDecimal quantity, BigDecimal averageCostBasis)
    {
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
    }

    //Getters and Setters
    public String getAccountId(){return accountId;}
    public void setAccountId(String accountId){ this.accountId = accountId; }
    
    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getAverageCostBasis() { return averageCostBasis; }
    public void setAverageCostBasis(BigDecimal averageCostBasis) { this.averageCostBasis = averageCostBasis; }
    


}
