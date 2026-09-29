package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public class EntirePortfolioResponseDTO {
    private String userId;
    private List<GetAccountPortfolioResponseDTO> accounts;
    
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalGainLossDollars;
    private BigDecimal totalGainLossPercent;
    
    // Constructor
    public EntirePortfolioResponseDTO(String userId, List<GetAccountPortfolioResponseDTO> accounts,
                                      BigDecimal totalPortfolioValue, BigDecimal totalCostBasis,
                                      BigDecimal totalGainLossDollars, BigDecimal totalGainLossPercent) {
        this.userId = userId;
        this.accounts = accounts;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalCostBasis = totalCostBasis;
        this.totalGainLossDollars = totalGainLossDollars;
        this.totalGainLossPercent = totalGainLossPercent;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<GetAccountPortfolioResponseDTO> getAccounts() {
        return accounts;
    }
    
    public void setAccounts(List<GetAccountPortfolioResponseDTO> accounts) {
        this.accounts = accounts;
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
