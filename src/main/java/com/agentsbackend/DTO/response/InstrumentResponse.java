package com.agentsbackend.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * InstrumentResponse - Wrapper for all market data API responses
 * Contains nested DTOs for different response types:
 * - SearchResult: Instrument search results
 * - InstrumentDetail: Full instrument information
 * - Quote: Current market quote
 * - HistoryResponse: Historical price data
 */
@Data
@NoArgsConstructor
public class InstrumentResponse {

    // ============================================================
    // SearchResult - Used by GET /instruments/search?q=
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResult {
        private String ticker;
        private String name;
        private String assetClass;
        private String industry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResultWrapper {
        private List<SearchResult> results;
    }

    // ============================================================
    // InstrumentDetail - Used by GET /instruments/{ticker}
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstrumentDetail {
        private String ticker;
        private String name;
        private String assetClass;
        private String industry;
    }

    // ============================================================
    // Quote - Used by GET /instruments/{ticker}/quote
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Quote {
        private String ticker;
        private BigDecimal bid;
        private BigDecimal ask;
        private BigDecimal lastPrice;
        private BigDecimal priceChange;
        private BigDecimal priceChangePercent;
        private Long volume;
        private LocalDateTime timestamp;
        private boolean isStale;  // true if returned from cache due to Alpaca unavailability
    }

    // ============================================================
    // HistoricalBar - Individual OHLCV bar for history response
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoricalBar {
        private LocalDateTime timestamp;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
    }

    // ============================================================
    // PeriodSummary - Statistical summary for historical data
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodSummary {
        private BigDecimal highest;
        private BigDecimal lowest;
        private BigDecimal startPrice;
        private BigDecimal endPrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Long avgVolume;
    }

    // ============================================================
    // HistoryResponse - Used by GET /instruments/{ticker}/history?period=
    // ============================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryResponse {
        private String ticker;
        private String period;  // 1D, 1W, 1M, 3M, 6M, 1Y, ALL
        private Integer barCount;
        private List<HistoricalBar> bars;  // Ordered DESC by timestamp
        
        @JsonProperty("summary")
        private PeriodSummary summary;
    }
}
