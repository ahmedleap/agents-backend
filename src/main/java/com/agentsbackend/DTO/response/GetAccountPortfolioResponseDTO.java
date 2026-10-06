package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public class GetAccountPortfolioResponseDTO {
    private String accountId;
    private String accountName;
    private List<GetHoldingResponseDTO> holdings;
    
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalGainLossDollars;
    private BigDecimal totalGainLossPercent;
    
    // Constructors
    public GetAccountPortfolioResponseDTO() {
    }
    
    public GetAccountPortfolioResponseDTO(String accountId, String accountName,
                                   List<GetHoldingResponseDTO> holdings,
                                   BigDecimal totalPortfolioValue,
                                   BigDecimal totalCostBasis,
                                   BigDecimal totalGainLossDollars,
                                   BigDecimal totalGainLossPercent) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.holdings = holdings;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalCostBasis = totalCostBasis;
        this.totalGainLossDollars = totalGainLossDollars;
        this.totalGainLossPercent = totalGainLossPercent;
    }
    
    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public List<GetHoldingResponseDTO> getHoldings() {
        return holdings;
    }
    
    public void setHoldings(List<GetHoldingResponseDTO> holdings) {
        this.holdings = holdings;
    }
    
    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }
    
    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }
    
    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }
    
    public void setTotalCostBasis(BigDecimal totalCostBasis) {
        this.totalCostBasis = totalCostBasis;
    }
    
    public BigDecimal getTotalGainLossDollars() {
        return totalGainLossDollars;
    }
    
    public void setTotalGainLossDollars(BigDecimal totalGainLossDollars) {
        this.totalGainLossDollars = totalGainLossDollars;
    }
    
    public BigDecimal getTotalGainLossPercent() {
        return totalGainLossPercent;
    }
    
    public void setTotalGainLossPercent(BigDecimal totalGainLossPercent) {
        this.totalGainLossPercent = totalGainLossPercent;
    }
}
