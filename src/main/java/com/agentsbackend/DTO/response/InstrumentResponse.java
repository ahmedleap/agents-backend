package com.agentsbackend.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * InstrumentResponse - Wrapper for all market data API responses.
 */
public class InstrumentResponse {

    public static class SearchResult {
        private UUID instrumentId;
        private String ticker;
        private String name;
        private String assetClass;
        private String industry;

        public SearchResult() {
        }

        public SearchResult(String ticker, String name, String assetClass, String industry) {
            this.ticker = ticker;
            this.name = name;
            this.assetClass = assetClass;
            this.industry = industry;
        }

        public SearchResult(UUID instrumentId, String ticker, String name, String assetClass, String industry) {
            this.instrumentId = instrumentId;
            this.ticker = ticker;
            this.name = name;
            this.assetClass = assetClass;
            this.industry = industry;
        }

        public static SearchResultBuilder builder() {
            return new SearchResultBuilder();
        }

        public String getTicker() {
            return ticker;
        }

        public UUID getInstrumentId() {
            return instrumentId;
        }

        public void setInstrumentId(UUID instrumentId) {
            this.instrumentId = instrumentId;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAssetClass() {
            return assetClass;
        }

        public void setAssetClass(String assetClass) {
            this.assetClass = assetClass;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public static class SearchResultBuilder {
            private UUID instrumentId;
            private String ticker;
            private String name;
            private String assetClass;
            private String industry;

            public SearchResultBuilder ticker(String ticker) {
                this.ticker = ticker;
                return this;
            }

            public SearchResultBuilder instrumentId(UUID instrumentId) {
                this.instrumentId = instrumentId;
                return this;
            }

            public SearchResultBuilder name(String name) {
                this.name = name;
                return this;
            }

            public SearchResultBuilder assetClass(String assetClass) {
                this.assetClass = assetClass;
                return this;
            }

            public SearchResultBuilder industry(String industry) {
                this.industry = industry;
                return this;
            }

            public SearchResult build() {
                return new SearchResult(instrumentId, ticker, name, assetClass, industry);
            }
        }
    }

    public static class SearchResultWrapper {
        private List<SearchResult> results;

        public SearchResultWrapper() {
        }

        public SearchResultWrapper(List<SearchResult> results) {
            this.results = results;
        }

        public static SearchResultWrapperBuilder builder() {
            return new SearchResultWrapperBuilder();
        }

        public List<SearchResult> getResults() {
            return results;
        }

        public void setResults(List<SearchResult> results) {
            this.results = results;
        }

        public static class SearchResultWrapperBuilder {
            private List<SearchResult> results;

            public SearchResultWrapperBuilder results(List<SearchResult> results) {
                this.results = results;
                return this;
            }

            public SearchResultWrapper build() {
                return new SearchResultWrapper(results);
            }
        }
    }

    public static class InstrumentWithQuote {
        private UUID instrumentId;
        private String ticker;
        private String name;
        private String assetClass;
        private String industry;
        private BigDecimal bid;
        private BigDecimal ask;
        private BigDecimal lastPrice;
        private LocalDateTime timestamp;
        private boolean stale;

        public InstrumentWithQuote() {
        }

        public InstrumentWithQuote(UUID instrumentId, String ticker, String name, String assetClass,
                                   String industry, BigDecimal bid, BigDecimal ask, BigDecimal lastPrice,
                                   LocalDateTime timestamp, boolean stale) {
            this.instrumentId = instrumentId;
            this.ticker = ticker;
            this.name = name;
            this.assetClass = assetClass;
            this.industry = industry;
            this.bid = bid;
            this.ask = ask;
            this.lastPrice = lastPrice;
            this.timestamp = timestamp;
            this.stale = stale;
        }

        public UUID getInstrumentId() { return instrumentId; }
        public String getTicker() { return ticker; }
        public String getName() { return name; }
        public String getAssetClass() { return assetClass; }
        public String getIndustry() { return industry; }
        public BigDecimal getBid() { return bid; }
        public BigDecimal getAsk() { return ask; }
        public BigDecimal getLastPrice() { return lastPrice; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isStale() { return stale; }

        public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public void setName(String name) { this.name = name; }
        public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
        public void setIndustry(String industry) { this.industry = industry; }
        public void setBid(BigDecimal bid) { this.bid = bid; }
        public void setAsk(BigDecimal ask) { this.ask = ask; }
        public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setStale(boolean stale) { this.stale = stale; }
    }

    public static class InstrumentDetail {
        private String ticker;
        private String name;
        private String assetClass;
        private String industry;

        public InstrumentDetail() {
        }

        public InstrumentDetail(String ticker, String name, String assetClass, String industry) {
            this.ticker = ticker;
            this.name = name;
            this.assetClass = assetClass;
            this.industry = industry;
        }

        public static InstrumentDetailBuilder builder() {
            return new InstrumentDetailBuilder();
        }

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAssetClass() {
            return assetClass;
        }

        public void setAssetClass(String assetClass) {
            this.assetClass = assetClass;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public static class InstrumentDetailBuilder {
            private String ticker;
            private String name;
            private String assetClass;
            private String industry;

            public InstrumentDetailBuilder ticker(String ticker) {
                this.ticker = ticker;
                return this;
            }

            public InstrumentDetailBuilder name(String name) {
                this.name = name;
                return this;
            }

            public InstrumentDetailBuilder assetClass(String assetClass) {
                this.assetClass = assetClass;
                return this;
            }

            public InstrumentDetailBuilder industry(String industry) {
                this.industry = industry;
                return this;
            }

            public InstrumentDetail build() {
                return new InstrumentDetail(ticker, name, assetClass, industry);
            }
        }
    }

    public static class Quote {
        private String ticker;
        private BigDecimal bid;
        private BigDecimal ask;
        private BigDecimal lastPrice;
        private BigDecimal priceChange;
        private BigDecimal priceChangePercent;
        private Long volume;
        private LocalDateTime timestamp;
        private boolean isStale;

        public Quote() {
        }

        public Quote(String ticker, BigDecimal bid, BigDecimal ask, BigDecimal lastPrice,
                     BigDecimal priceChange, BigDecimal priceChangePercent, Long volume,
                     LocalDateTime timestamp, boolean isStale) {
            this.ticker = ticker;
            this.bid = bid;
            this.ask = ask;
            this.lastPrice = lastPrice;
            this.priceChange = priceChange;
            this.priceChangePercent = priceChangePercent;
            this.volume = volume;
            this.timestamp = timestamp;
            this.isStale = isStale;
        }

        public static QuoteBuilder builder() {
            return new QuoteBuilder();
        }

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public BigDecimal getBid() {
            return bid;
        }

        public void setBid(BigDecimal bid) {
            this.bid = bid;
        }

        public BigDecimal getAsk() {
            return ask;
        }

        public void setAsk(BigDecimal ask) {
            this.ask = ask;
        }

        public BigDecimal getLastPrice() {
            return lastPrice;
        }

        public void setLastPrice(BigDecimal lastPrice) {
            this.lastPrice = lastPrice;
        }

        public BigDecimal getPriceChange() {
            return priceChange;
        }

        public void setPriceChange(BigDecimal priceChange) {
            this.priceChange = priceChange;
        }

        public BigDecimal getPriceChangePercent() {
            return priceChangePercent;
        }

        public void setPriceChangePercent(BigDecimal priceChangePercent) {
            this.priceChangePercent = priceChangePercent;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isStale() {
            return isStale;
        }

        public void setStale(boolean stale) {
            isStale = stale;
        }

        public static class QuoteBuilder {
            private String ticker;
            private BigDecimal bid;
            private BigDecimal ask;
            private BigDecimal lastPrice;
            private BigDecimal priceChange;
            private BigDecimal priceChangePercent;
            private Long volume;
            private LocalDateTime timestamp;
            private boolean isStale;

            public QuoteBuilder ticker(String ticker) {
                this.ticker = ticker;
                return this;
            }

            public QuoteBuilder bid(BigDecimal bid) {
                this.bid = bid;
                return this;
            }

            public QuoteBuilder ask(BigDecimal ask) {
                this.ask = ask;
                return this;
            }

            public QuoteBuilder lastPrice(BigDecimal lastPrice) {
                this.lastPrice = lastPrice;
                return this;
            }

            public QuoteBuilder priceChange(BigDecimal priceChange) {
                this.priceChange = priceChange;
                return this;
            }

            public QuoteBuilder priceChangePercent(BigDecimal priceChangePercent) {
                this.priceChangePercent = priceChangePercent;
                return this;
            }

            public QuoteBuilder volume(Long volume) {
                this.volume = volume;
                return this;
            }

            public QuoteBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public QuoteBuilder isStale(boolean isStale) {
                this.isStale = isStale;
                return this;
            }

            public Quote build() {
                return new Quote(
                    ticker,
                    bid,
                    ask,
                    lastPrice,
                    priceChange,
                    priceChangePercent,
                    volume,
                    timestamp,
                    isStale
                );
            }
        }
    }

    public static class HistoricalBar {
        private LocalDateTime timestamp;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;

        public HistoricalBar() {
        }

        public HistoricalBar(LocalDateTime timestamp, BigDecimal open, BigDecimal high,
                             BigDecimal low, BigDecimal close, Long volume) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }

        public static HistoricalBarBuilder builder() {
            return new HistoricalBarBuilder();
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public BigDecimal getOpen() {
            return open;
        }

        public void setOpen(BigDecimal open) {
            this.open = open;
        }

        public BigDecimal getHigh() {
            return high;
        }

        public void setHigh(BigDecimal high) {
            this.high = high;
        }

        public BigDecimal getLow() {
            return low;
        }

        public void setLow(BigDecimal low) {
            this.low = low;
        }

        public BigDecimal getClose() {
            return close;
        }

        public void setClose(BigDecimal close) {
            this.close = close;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public static class HistoricalBarBuilder {
            private LocalDateTime timestamp;
            private BigDecimal open;
            private BigDecimal high;
            private BigDecimal low;
            private BigDecimal close;
            private Long volume;

            public HistoricalBarBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public HistoricalBarBuilder open(BigDecimal open) {
                this.open = open;
                return this;
            }

            public HistoricalBarBuilder high(BigDecimal high) {
                this.high = high;
                return this;
            }

            public HistoricalBarBuilder low(BigDecimal low) {
                this.low = low;
                return this;
            }

            public HistoricalBarBuilder close(BigDecimal close) {
                this.close = close;
                return this;
            }

            public HistoricalBarBuilder volume(Long volume) {
                this.volume = volume;
                return this;
            }

            public HistoricalBar build() {
                return new HistoricalBar(timestamp, open, high, low, close, volume);
            }
        }
    }

    public static class PeriodSummary {
        private BigDecimal highest;
        private BigDecimal lowest;
        private BigDecimal startPrice;
        private BigDecimal endPrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Long avgVolume;

        public PeriodSummary() {
        }

        public PeriodSummary(BigDecimal highest, BigDecimal lowest, BigDecimal startPrice, BigDecimal endPrice,
                             BigDecimal change, BigDecimal changePercent, Long avgVolume) {
            this.highest = highest;
            this.lowest = lowest;
            this.startPrice = startPrice;
            this.endPrice = endPrice;
            this.change = change;
            this.changePercent = changePercent;
            this.avgVolume = avgVolume;
        }

        public static PeriodSummaryBuilder builder() {
            return new PeriodSummaryBuilder();
        }

        public BigDecimal getHighest() {
            return highest;
        }

        public void setHighest(BigDecimal highest) {
            this.highest = highest;
        }

        public BigDecimal getLowest() {
            return lowest;
        }

        public void setLowest(BigDecimal lowest) {
            this.lowest = lowest;
        }

        public BigDecimal getStartPrice() {
            return startPrice;
        }

        public void setStartPrice(BigDecimal startPrice) {
            this.startPrice = startPrice;
        }

        public BigDecimal getEndPrice() {
            return endPrice;
        }

        public void setEndPrice(BigDecimal endPrice) {
            this.endPrice = endPrice;
        }

        public BigDecimal getChange() {
            return change;
        }

        public void setChange(BigDecimal change) {
            this.change = change;
        }

        public BigDecimal getChangePercent() {
            return changePercent;
        }

        public void setChangePercent(BigDecimal changePercent) {
            this.changePercent = changePercent;
        }

        public Long getAvgVolume() {
            return avgVolume;
        }

        public void setAvgVolume(Long avgVolume) {
            this.avgVolume = avgVolume;
        }

        public static class PeriodSummaryBuilder {
            private BigDecimal highest;
            private BigDecimal lowest;
            private BigDecimal startPrice;
            private BigDecimal endPrice;
            private BigDecimal change;
            private BigDecimal changePercent;
            private Long avgVolume;

            public PeriodSummaryBuilder highest(BigDecimal highest) {
                this.highest = highest;
                return this;
            }

            public PeriodSummaryBuilder lowest(BigDecimal lowest) {
                this.lowest = lowest;
                return this;
            }

            public PeriodSummaryBuilder startPrice(BigDecimal startPrice) {
                this.startPrice = startPrice;
                return this;
            }

            public PeriodSummaryBuilder endPrice(BigDecimal endPrice) {
                this.endPrice = endPrice;
                return this;
            }

            public PeriodSummaryBuilder change(BigDecimal change) {
                this.change = change;
                return this;
            }

            public PeriodSummaryBuilder changePercent(BigDecimal changePercent) {
                this.changePercent = changePercent;
                return this;
            }

            public PeriodSummaryBuilder avgVolume(Long avgVolume) {
                this.avgVolume = avgVolume;
                return this;
            }

            public PeriodSummary build() {
                return new PeriodSummary(highest, lowest, startPrice, endPrice, change, changePercent, avgVolume);
            }
        }
    }

    public static class HistoryResponse {
        private String ticker;
        private String period;
        private Integer barCount;
        private List<HistoricalBar> bars;

        @JsonProperty("summary")
        private PeriodSummary summary;

        public HistoryResponse() {
        }

        public HistoryResponse(String ticker, String period, Integer barCount,
                               List<HistoricalBar> bars, PeriodSummary summary) {
            this.ticker = ticker;
            this.period = period;
            this.barCount = barCount;
            this.bars = bars;
            this.summary = summary;
        }

        public static HistoryResponseBuilder builder() {
            return new HistoryResponseBuilder();
        }

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public Integer getBarCount() {
            return barCount;
        }

        public void setBarCount(Integer barCount) {
            this.barCount = barCount;
        }

        public List<HistoricalBar> getBars() {
            return bars;
        }

        public void setBars(List<HistoricalBar> bars) {
            this.bars = bars;
        }

        public PeriodSummary getSummary() {
            return summary;
        }

        public void setSummary(PeriodSummary summary) {
            this.summary = summary;
        }

        public static class HistoryResponseBuilder {
            private String ticker;
            private String period;
            private Integer barCount;
            private List<HistoricalBar> bars;
            private PeriodSummary summary;

            public HistoryResponseBuilder ticker(String ticker) {
                this.ticker = ticker;
                return this;
            }

            public HistoryResponseBuilder period(String period) {
                this.period = period;
                return this;
            }

            public HistoryResponseBuilder barCount(Integer barCount) {
                this.barCount = barCount;
                return this;
            }

            public HistoryResponseBuilder bars(List<HistoricalBar> bars) {
                this.bars = bars;
                return this;
            }

            public HistoryResponseBuilder summary(PeriodSummary summary) {
                this.summary = summary;
                return this;
            }

            public HistoryResponse build() {
                return new HistoryResponse(ticker, period, barCount, bars, summary);
            }
        }
    }
}
