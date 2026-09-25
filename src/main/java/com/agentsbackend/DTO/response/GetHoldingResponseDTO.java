package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

public class GetHoldingResponseDTO {
    private String holdingId;
    private String accountId;
    private String instrumentId;
    private String ticker;
    private String name;
    private BigDecimal quantity;
    private BigDecimal averageCostBasis;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal gainLossDollars;
    private BigDecimal gainLossPercent;

    //constructor - data container only, no calculations
    public GetHoldingResponseDTO(String holdingId, String accountId, String instrumentId, 
                                 String ticker, String name, BigDecimal quantity, 
                                 BigDecimal averageCostBasis, BigDecimal currentPrice,
                                 BigDecimal currentValue, BigDecimal gainLossDollars,
                                 BigDecimal gainLossPercent){
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.ticker = ticker;
        this.name = name;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.gainLossDollars = gainLossDollars;
        this.gainLossPercent = gainLossPercent;
    }

    //Getters and Setters
    public String getHoldingId() {return holdingId;}
    public void setHoldingId(String holdingId){this.holdingId = holdingId;}

    public String getAccountId() {return accountId;}
    public void setAccountId(String accountId){this.accountId = accountId;}

    public String getInstrumentId() {return instrumentId;}
    public void setInstrumentId(String instrumentId){this.instrumentId = instrumentId;}

    public String getTicker() {return ticker;}
    public void setTicker(String ticker){this.ticker = ticker;}

    public String getName() {return name;}
    public void setName(String name){this.name = name;}

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity( BigDecimal quantity ) { this.quantity = quantity; }

    public BigDecimal getAverageCostBasis() { return averageCostBasis; }
    public void setAverageCostBasis( BigDecimal averageCostBasis ) { this.averageCostBasis = averageCostBasis; }

    public BigDecimal getCurrentPrice() {return currentPrice;}
    public void setCurrentPrice(BigDecimal currentPrice){this.currentPrice = currentPrice;}

    public BigDecimal getCurrentValue() {return currentValue;}
    public void setCurrentValue(BigDecimal currentValue){this.currentValue = currentValue;}

    public BigDecimal getGainLossDollars() {return gainLossDollars;}
    public void setGainLossDollars(BigDecimal gainLossDollars){this.gainLossDollars = gainLossDollars;}

    public BigDecimal getGainLossPercent() {return gainLossPercent;}
    public void setGainLossPercent(BigDecimal gainLossPercent){this.gainLossPercent = gainLossPercent;}
}
