package com.agentsbackend.DTO.response;

import java.util.UUID;
import java.math.BigDecimal;

/**
 * Client segmentation analytics response DTO.
 * Contains segmented client metrics by risk tolerance, portfolio size, or other dimensions.
 */
public class ClientSegmentationResponse {
    private String segment;
    private String dimension;  // e.g., "RISK_TOLERANCE", "PORTFOLIO_SIZE_RANGE"
    private Long clientCount;
    private BigDecimal averagePortfolioValue;
    private BigDecimal totalAssetsUnderManagement;
    private BigDecimal averageCashBalance;
    private Long totalOrders;
    private BigDecimal orderSuccessRate;
    private Long avgOrdersPerClient;
    private BigDecimal avgAccountAge;

    public ClientSegmentationResponse() {}

    public ClientSegmentationResponse(String segment, String dimension, Long clientCount) {
        this.segment = segment;
        this.dimension = dimension;
        this.clientCount = clientCount;
    }

    // Getters and Setters
    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public Long getClientCount() {
        return clientCount;
    }

    public void setClientCount(Long clientCount) {
        this.clientCount = clientCount;
    }

    public BigDecimal getAveragePortfolioValue() {
        return averagePortfolioValue;
    }

    public void setAveragePortfolioValue(BigDecimal averagePortfolioValue) {
        this.averagePortfolioValue = averagePortfolioValue;
    }

    public BigDecimal getTotalAssetsUnderManagement() {
        return totalAssetsUnderManagement;
    }

    public void setTotalAssetsUnderManagement(BigDecimal totalAssetsUnderManagement) {
        this.totalAssetsUnderManagement = totalAssetsUnderManagement;
    }

    public BigDecimal getAverageCashBalance() {
        return averageCashBalance;
    }

    public void setAverageCashBalance(BigDecimal averageCashBalance) {
        this.averageCashBalance = averageCashBalance;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getOrderSuccessRate() {
        return orderSuccessRate;
    }

    public void setOrderSuccessRate(BigDecimal orderSuccessRate) {
        this.orderSuccessRate = orderSuccessRate;
    }

    public Long getAvgOrdersPerClient() {
        return avgOrdersPerClient;
    }

    public void setAvgOrdersPerClient(Long avgOrdersPerClient) {
        this.avgOrdersPerClient = avgOrdersPerClient;
    }

    public BigDecimal getAvgAccountAge() {
        return avgAccountAge;
    }

    public void setAvgAccountAge(BigDecimal avgAccountAge) {
        this.avgAccountAge = avgAccountAge;
    }
}
