package com.agentsbackend.services;

import com.agentsbackend.DTO.response.InstrumentResponse;
import java.util.List;

/**
 * Service interface for Instrument and market data operations.
 * Defines business logic contracts for instrument search, details retrieval,
 * real-time quote fetching, and historical price queries.
 *
 * Caching strategy:
 * - Search results: NOT cached (database only, for typeahead)
 * - Instrument details: 1 hour cache TTL
 * - Quotes: 15-30 second cache TTL (real-time freshness)
 * - Historical prices: NOT cached (data is static, user requests variable ranges)
 */
public interface InstrumentService {

    /**
     * Search for instruments by ticker or name.
     * Used by GET /instruments/search?q=
     * No caching - direct database query for typeahead performance.
     *
     * @param query Search query (1-50 characters: ticker or company name)
     * @return InstrumentResponse.SearchResultWrapper with matching instruments
     * @throws IllegalArgumentException if query is null or invalid length
     */
    InstrumentResponse.SearchResultWrapper searchInstruments(String query);

    /**
     * Get detailed instrument information including company profile.
     * Used by GET /instruments/{ticker}
     * Cached for 1 hour (asset class, industry, shares outstanding don't change frequently).
     *
     * @param ticker Stock ticker symbol (e.g., "AAPL")
     * @return InstrumentResponse.InstrumentDetail with full instrument details
     * @throws IllegalArgumentException if instrument not found
     */
    InstrumentResponse.InstrumentDetail getInstrumentDetails(String ticker);

    /**
     * Get current market quote for an instrument.
     * Used by GET /instruments/{ticker}/quote
     * Cached for 15-30 seconds (real-time but respects API rate limits).
     *
     * Calls Alpaca API to fetch latest bid/ask/lastPrice.
     * On Alpaca unavailability, returns last cached value with isStale=true.
     *
     * @param ticker Stock ticker symbol (e.g., "AAPL")
     * @return InstrumentResponse.Quote with current market data
     * @throws IllegalArgumentException if instrument not found
     * @throws RuntimeException if Alpaca API fails AND no cache available
     */
    InstrumentResponse.Quote getCurrentQuote(String ticker);

    /**
     * Get historical OHLCV price data for an instrument.
     * Used by GET /instruments/{ticker}/history?period=
     * NOT cached (data is static, queries request variable time ranges).
     *
     * @param ticker Stock ticker symbol (e.g., "AAPL")
     * @param period Time period: 1D, 1W, 1M, 3M, 6M, 1Y, ALL (default: 1Y)
     * @return InstrumentResponse.HistoryResponse with bars ordered DESC by timestamp
     * @throws IllegalArgumentException if instrument not found or period invalid
     */
    InstrumentResponse.HistoryResponse getHistoricalPrices(String ticker, String period);
}
