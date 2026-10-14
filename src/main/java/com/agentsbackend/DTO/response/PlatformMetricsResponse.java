package com.agentsbackend.DTO.response;

import java.math.BigDecimal;

/**
 * Platform-wide trading metrics aggregated across all clients and accounts.
 */
public class PlatformMetricsResponse {
    private Long totalClients;
    private Long activeClientsLast30Days;
    private Long totalAccounts;
    private Long activeAccountsLast30Days;
    private BigDecimal totalAssetsUnderManagement;
    private BigDecimal averagePortfolioSize;
    private Long totalOrders;
    private Long totalFilledOrders;
    private Long totalCancelledOrders;
    private BigDecimal orderFulfillmentRate;
    private BigDecimal orderCancellationRate;
    private BigDecimal totalVolumeTraded;
    private BigDecimal totalValueTraded;
    private BigDecimal averageOrderValue;
    private BigDecimal averageOrderQuantity;

    public PlatformMetricsResponse() {}

    // Getters and Setters
    public Long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(Long totalClients) {
        this.totalClients = totalClients;
    }

    public Long getActiveClientsLast30Days() {
        return activeClientsLast30Days;
    }

    public void setActiveClientsLast30Days(Long activeClientsLast30Days) {
        this.activeClientsLast30Days = activeClientsLast30Days;
    }

    public Long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(Long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public Long getActiveAccountsLast30Days() {
        return activeAccountsLast30Days;
    }

    public void setActiveAccountsLast30Days(Long activeAccountsLast30Days) {
        this.activeAccountsLast30Days = activeAccountsLast30Days;
    }

    public BigDecimal getTotalAssetsUnderManagement() {
        return totalAssetsUnderManagement;
    }

    public void setTotalAssetsUnderManagement(BigDecimal totalAssetsUnderManagement) {
        this.totalAssetsUnderManagement = totalAssetsUnderManagement;
    }

    public BigDecimal getAveragePortfolioSize() {
        return averagePortfolioSize;
    }

    public void setAveragePortfolioSize(BigDecimal averagePortfolioSize) {
        this.averagePortfolioSize = averagePortfolioSize;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalFilledOrders() {
        return totalFilledOrders;
    }

    public void setTotalFilledOrders(Long totalFilledOrders) {
        this.totalFilledOrders = totalFilledOrders;
    }

    public Long getTotalCancelledOrders() {
        return totalCancelledOrders;
    }

    public void setTotalCancelledOrders(Long totalCancelledOrders) {
        this.totalCancelledOrders = totalCancelledOrders;
    }

    public BigDecimal getOrderFulfillmentRate() {
        return orderFulfillmentRate;
    }

    public void setOrderFulfillmentRate(BigDecimal orderFulfillmentRate) {
        this.orderFulfillmentRate = orderFulfillmentRate;
    }

    public BigDecimal getOrderCancellationRate() {
        return orderCancellationRate;
    }

    public void setOrderCancellationRate(BigDecimal orderCancellationRate) {
        this.orderCancellationRate = orderCancellationRate;
    }

    public BigDecimal getTotalVolumeTraded() {
        return totalVolumeTraded;
    }

    public void setTotalVolumeTraded(BigDecimal totalVolumeTraded) {
        this.totalVolumeTraded = totalVolumeTraded;
    }

    public BigDecimal getTotalValueTraded() {
        return totalValueTraded;
    }

    public void setTotalValueTraded(BigDecimal totalValueTraded) {
        this.totalValueTraded = totalValueTraded;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public BigDecimal getAverageOrderQuantity() {
        return averageOrderQuantity;
    }

    public void setAverageOrderQuantity(BigDecimal averageOrderQuantity) {
        this.averageOrderQuantity = averageOrderQuantity;
    }
}
