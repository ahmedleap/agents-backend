package com.agentsbackend.DTO.response;

import java.time.OffsetDateTime;

/**
 * One time bucket of trading volume for the intraday dashboard chart.
 */
public class IntradayVolumeResponse {
    private OffsetDateTime bucketStart;
    private Long totalQuantityTraded;
    private Double totalValueTraded;
    private Long tradeCount;
    private Long buyCount;
    private Long sellCount;

    public IntradayVolumeResponse() {
    }

    public IntradayVolumeResponse(OffsetDateTime bucketStart, Long totalQuantityTraded,
                                  Double totalValueTraded, Long tradeCount,
                                  Long buyCount, Long sellCount) {
        this.bucketStart = bucketStart;
        this.totalQuantityTraded = totalQuantityTraded;
        this.totalValueTraded = totalValueTraded;
        this.tradeCount = tradeCount;
        this.buyCount = buyCount;
        this.sellCount = sellCount;
    }

    public OffsetDateTime getBucketStart() {
        return bucketStart;
    }

    public void setBucketStart(OffsetDateTime bucketStart) {
        this.bucketStart = bucketStart;
    }

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

    public Long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Long getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(Long buyCount) {
        this.buyCount = buyCount;
    }

    public Long getSellCount() {
        return sellCount;
    }

    public void setSellCount(Long sellCount) {
        this.sellCount = sellCount;
    }
}
