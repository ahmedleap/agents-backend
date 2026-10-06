package com.agentsbackend.services;

import com.agentsbackend.DTO.response.InstrumentResponse;
import com.agentsbackend.repos.InstrumentRepository;
import com.agentsbackend.repos.InstrumentPriceHistoryRepository;
import com.agentsbackend.entities.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Instrument and market data operations.
 * Implements business logic for instrument search, details, quotes, and historical data.
 *
 * Caching configuration:
 * - quotes cache: 15-30 second TTL (real-time with rate limit protection)
 * - instruments cache: 1 hour TTL (static metadata)
 *
 * External integration:
 * - AlpacaClient: Fetches real-time quotes from Alpaca market data API
 * - Falls back to cached value if Alpaca is unavailable
 */
@Service
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentPriceHistoryRepository priceHistoryRepository;
    // AlpacaClient will be injected after Phase 5 is created

    public InstrumentServiceImpl(InstrumentRepository instrumentRepository,
                               InstrumentPriceHistoryRepository priceHistoryRepository) {
        this.instrumentRepository = instrumentRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    /**
     * Search instruments by ticker or name (no caching).
     * Direct database query for fast typeahead response.
     */
    @Override
    public InstrumentResponse.SearchResultWrapper searchInstruments(String query) {
        if (query == null || query.trim().isEmpty() || query.length() > 50) {
            throw new IllegalArgumentException("Search query must be 1-50 characters");
        }

        List<Instrument> results = instrumentRepository.searchByQuery(query.trim());
        
        List<InstrumentResponse.SearchResult> searchResults = results.stream()
            .map(instrument -> InstrumentResponse.SearchResult.builder()
                .ticker(instrument.getTicker())
                .name(instrument.getName())
                .assetClass(instrument.getAssetClass() != null ? instrument.getAssetClass().toString() : null)
                .industry(instrument.getIndustry())
                .build())
            .collect(Collectors.toList());

        return new InstrumentResponse.SearchResultWrapper(searchResults);
    }

    /**
     * Get instrument details with 1-hour cache.
     * Returns asset class, industry, and security profile information.
     */
    @Override
    @Cacheable(value = "instruments", key = "#ticker", unless = "#result == null")
    public InstrumentResponse.InstrumentDetail getInstrumentDetails(String ticker) {
        Instrument instrument = instrumentRepository.findByTicker(ticker.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker));

        return InstrumentResponse.InstrumentDetail.builder()
            .ticker(instrument.getTicker())
            .name(instrument.getName())
            .assetClass(instrument.getAssetClass().toString())
            .industry(instrument.getIndustry())
            .build();
    }

    /**
     * Get current market quote with 15-30 second cache.
     * Calls Alpaca API for real-time data.
     * Falls back to cached value with isStale=true if API unavailable.
     */
    @Override
    @Cacheable(value = "quotes", key = "#ticker", unless = "#result == null")
    public InstrumentResponse.Quote getCurrentQuote(String ticker) {
        Instrument instrument = instrumentRepository.findByTicker(ticker.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker));

        // TODO: In Phase 5 (Alpaca integration), call AlpacaClient.getLatestQuote(ticker)
        // For now, return database values with isStale=true
        boolean isStale = instrument.getPriceUpdatedAt() == null || 
                         OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1).isAfter(instrument.getPriceUpdatedAt());

        return InstrumentResponse.Quote.builder()
            .ticker(instrument.getTicker())
            .bid(instrument.getBid())
            .ask(instrument.getAsk())
            .lastPrice(instrument.getAsk() != null ? instrument.getAsk() : BigDecimal.ZERO)
            .priceChange(BigDecimal.ZERO)
            .priceChangePercent(BigDecimal.ZERO)
            .volume(0L)
            .timestamp(instrument.getPriceUpdatedAt() != null ? instrument.getPriceUpdatedAt().toLocalDateTime() : null)
            .isStale(isStale)
            .build();
    }

    /**
     * Get historical price data for a date range.
     * Not cached (static data, variable query ranges).
     */
    @Override
    public InstrumentResponse.HistoryResponse getHistoricalPrices(String ticker, String period) {
        Instrument instrument = instrumentRepository.findByTicker(ticker.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker));

        // Calculate date range based on period
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startTime;

        switch (period != null ? period.toUpperCase() : "1Y") {
            case "1D":
                startTime = endTime.minusDays(1);
                break;
            case "1W":
                startTime = endTime.minusWeeks(1);
                break;
            case "1M":
                startTime = endTime.minusMonths(1);
                break;
            case "3M":
                startTime = endTime.minusMonths(3);
                break;
            case "6M":
                startTime = endTime.minusMonths(6);
                break;
            case "1Y":
                startTime = endTime.minusYears(1);
                break;
            case "ALL":
                startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period + ". Must be: 1D, 1W, 1M, 3M, 6M, 1Y, or ALL");
        }

        // Fetch historical price data from database
        List<InstrumentResponse.HistoricalBar> bars = priceHistoryRepository.findHistoricalPrices(
                instrument.getInstrumentId(),
                startTime,
                endTime
            ).stream()
            .map(priceHistory -> InstrumentResponse.HistoricalBar.builder()
                .timestamp(priceHistory.getTimestamp() != null ? priceHistory.getTimestamp().toLocalDateTime() : null)
                .open(priceHistory.getOpen())
                .high(priceHistory.getHigh())
                .low(priceHistory.getLow())
                .close(priceHistory.getClose())
                .volume(priceHistory.getVolume() != null ? priceHistory.getVolume().longValue() : 0L)
                .build())
            .toList();

        // Calculate summary statistics
        BigDecimal highest = bars.isEmpty() ? BigDecimal.ZERO : 
            bars.stream().map(InstrumentResponse.HistoricalBar::getHigh)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal lowest = bars.isEmpty() ? BigDecimal.ZERO : 
            bars.stream().map(InstrumentResponse.HistoricalBar::getLow)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal startPrice = bars.isEmpty() ? BigDecimal.ZERO : bars.get(bars.size() - 1).getOpen();
        BigDecimal endPrice = bars.isEmpty() ? BigDecimal.ZERO : bars.get(0).getClose();
        BigDecimal change = endPrice.subtract(startPrice);
        BigDecimal changePercent = startPrice.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
            change.divide(startPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        
        Long avgVolume = bars.isEmpty() ? 0L : 
            (long) bars.stream().mapToLong(InstrumentResponse.HistoricalBar::getVolume).average().orElse(0L);

        InstrumentResponse.PeriodSummary summary = InstrumentResponse.PeriodSummary.builder()
            .highest(highest)
            .lowest(lowest)
            .startPrice(startPrice)
            .endPrice(endPrice)
            .change(change)
            .changePercent(changePercent)
            .avgVolume(avgVolume)
            .build();

        return InstrumentResponse.HistoryResponse.builder()
            .ticker(instrument.getTicker())
            .period(period != null ? period.toUpperCase() : "1Y")
            .barCount(bars.size())
            .bars(bars)
            .summary(summary)
            .build();
    }
}
