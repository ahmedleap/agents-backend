package com.agentsbackend.DTO.response;

/**
 * TotalVolumeResponse - DTO for total trading volume metric.
 * Represents the total quantity and value of all trades on the platform.
 */
public class TotalVolumeResponse {
    private Long totalQuantityTraded;
    private Double totalValueTraded;
    private Long totalOrdersExecuted;
    private Double averageOrderQuantity;
    private Double averageOrderValue;

    // Constructors
    public TotalVolumeResponse() {}

    public TotalVolumeResponse(Long totalQuantityTraded, Double totalValueTraded, Long totalOrdersExecuted, 
                             Double averageOrderQuantity, Double averageOrderValue) {
        this.totalQuantityTraded = totalQuantityTraded;
        this.totalValueTraded = totalValueTraded;
        this.totalOrdersExecuted = totalOrdersExecuted;
        this.averageOrderQuantity = averageOrderQuantity;
        this.averageOrderValue = averageOrderValue;
    }

    // Getters and Setters
    public Long getTotalQuantityTraded() {
        return totalQuantityTraded;
    }

    public void setTotalQuantityTraded(Long totalQuantityTraded) {
        this.totalQuantityTraded = totalQuantityTraded;
    }

    public Double getTotalValueTraded() {
        return totalValueTraded;
    }

    public void setTotalValueTraded(Double totalValueTraded) {
        this.totalValueTraded = totalValueTraded;
    }

    public Long getTotalOrdersExecuted() {
        return totalOrdersExecuted;
    }

    public void setTotalOrdersExecuted(Long totalOrdersExecuted) {
        this.totalOrdersExecuted = totalOrdersExecuted;
    }

    public Double getAverageOrderQuantity() {
        return averageOrderQuantity;
    }

    public void setAverageOrderQuantity(Double averageOrderQuantity) {
        this.averageOrderQuantity = averageOrderQuantity;
    }

    public Double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    @Override
    public String toString() {
        return "TotalVolumeResponse{" +
                "totalQuantityTraded=" + totalQuantityTraded +
                ", totalValueTraded=" + totalValueTraded +
                ", totalOrdersExecuted=" + totalOrdersExecuted +
                ", averageOrderQuantity=" + averageOrderQuantity +
                ", averageOrderValue=" + averageOrderValue +
                '}';
    }
}
