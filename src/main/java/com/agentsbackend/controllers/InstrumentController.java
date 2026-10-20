package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.InstrumentResponse;
import com.agentsbackend.services.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Market Data API endpoints.
 * Provides 4 endpoints for instrument search, details, quotes, and historical prices.
 *
 * Endpoints:
 * 1. GET /api/instruments/search?q=aapl           -> Search results
 * 2. GET /api/instruments/{ticker}                -> Instrument details
 * 3. GET /api/instruments/{ticker}/quote          -> Real-time quote
 * 4. GET /api/instruments/{ticker}/history?period=1Y -> Historical prices
 */
@RestController
@RequestMapping("/api/instruments")
@Tag(name = "Instruments", description = "Instrument search, quotes, and historical market data")
@SecurityRequirement(name = "bearerAuth")
public class InstrumentController {

    private static final Logger log = LoggerFactory.getLogger(InstrumentController.class);

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    /**
     * List all instruments for the trades page.
     * GET /api/instruments
     */
    @GetMapping
    @Operation(summary = "List all instruments", description = "Returns all available instruments ordered by ticker. Use the search endpoint for filtering.")
    @ApiResponse(responseCode = "200", description = "All instruments returned")
    public ResponseEntity<InstrumentResponse.SearchResultWrapper> listInstruments() {
        return ResponseEntity.ok(instrumentService.getAllInstruments());
    }

    @GetMapping("/with-quotes")
    @Operation(summary = "List instruments with latest quotes", description = "Returns all instruments and their latest stored quote in one request. Prefer this endpoint when loading the trades page.")
    @ApiResponse(responseCode = "200", description = "Instruments and latest quotes returned")
    public ResponseEntity<List<InstrumentResponse.InstrumentWithQuote>> listInstrumentsWithQuotes() {
        return ResponseEntity.ok(instrumentService.getAllInstrumentsWithQuotes());
    }

    /**
     * Search for instruments by ticker or company name.
     * GET /api/instruments/search?q=aapl
     *
     * Query Parameters:
     * - q (required): Search query (1-50 characters, case-insensitive)
     *   Example: "AAPL", "apple", "MSFT", "microsoft"
     *
     * Response: 200 OK
     * {
     *   "results": [
     *     {"ticker": "AAPL", "name": "Apple Inc.", "assetClass": "STOCK", "industry": "Technology"},
     *     ...
     *   ]
     * }
     *
     * @param q Search query string (ticker or company name)
     * @return ResponseEntity with SearchResultWrapper (HTTP 200)
     * @throws IllegalArgumentException if query is null or invalid length
     */
    @GetMapping("/search")
    @Operation(summary = "Search instruments", description = "Searches instruments by ticker or company name.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "400", description = "Invalid search query")
    })
    public ResponseEntity<InstrumentResponse.SearchResultWrapper> searchInstruments(
            @Parameter(description = "Ticker or company name to search for", example = "AAPL", required = true)
            @RequestParam(value = "q") String q) {
        log.debug("Search instruments with query: {}", q);
        
        try {
            InstrumentResponse.SearchResultWrapper results = instrumentService.searchInstruments(q);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid search query: {}", q, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get detailed information for a specific instrument.
     * GET /api/instruments/{ticker}
     *
     * Path Parameters:
     * - ticker: Stock ticker symbol (e.g., "AAPL", "MSFT", "GOOGL")
     *
     * Response: 200 OK
     * {
     *   "ticker": "AAPL",
     *   "name": "Apple Inc.",
     *   "assetClass": "STOCK",
     *   "industry": "Technology",
     *   "securityProfile": {
     *     "sharesOutstanding": 15600000000,
     *     "marketCap": 2800000000000,
     *     "dividendYield": 0.0045
     *   }
     * }
     *
     * @param ticker Stock ticker symbol (case-insensitive, will be converted to uppercase)
     * @return ResponseEntity with InstrumentDetail (HTTP 200) or 404 if not found
     */
    @GetMapping("/{ticker}")
    @Operation(summary = "Get instrument details", description = "Returns detailed information for an instrument ticker.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Instrument details returned"),
        @ApiResponse(responseCode = "404", description = "Instrument not found")
    })
    public ResponseEntity<InstrumentResponse.InstrumentDetail> getInstrumentDetails(
            @Parameter(description = "Instrument ticker symbol", example = "AAPL", required = true)
            @PathVariable("ticker") String ticker) {
        log.debug("Get instrument details for ticker: {}", ticker);
        
        try {
            InstrumentResponse.InstrumentDetail details = instrumentService.getInstrumentDetails(ticker);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            log.warn("Instrument not found: {}", ticker);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get current market quote for an instrument (real-time pricing).
     * GET /api/instruments/{ticker}/quote
     *
     * Path Parameters:
     * - ticker: Stock ticker symbol (e.g., "AAPL", "MSFT")
     *
     * Response: 200 OK
     * {
     *   "ticker": "AAPL",
     *   "bid": 227.50,
     *   "ask": 227.75,
     *   "lastPrice": 227.75,
     *   "priceChange": 1.25,
     *   "priceChangePercent": 0.55,
     *   "volume": 45230000,
     *   "timestamp": "2026-09-29T16:30:00Z",
     *   "isStale": false
     * }
     *
     * Note: If Alpaca API is unavailable, returns cached data with isStale=true
     *
     * @param ticker Stock ticker symbol (case-insensitive, will be converted to uppercase)
     * @return ResponseEntity with Quote (HTTP 200) or 404 if not found
     */
    @GetMapping("/{ticker}/quote")
    @Operation(summary = "Get current quote", description = "Returns the current market quote, using cached data when the upstream market data provider is unavailable.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current quote returned"),
        @ApiResponse(responseCode = "404", description = "Instrument not found"),
        @ApiResponse(responseCode = "503", description = "Market data service unavailable")
    })
    public ResponseEntity<InstrumentResponse.Quote> getCurrentQuote(
            @Parameter(description = "Instrument ticker symbol", example = "AAPL", required = true)
            @PathVariable("ticker") String ticker) {
        log.debug("Get current quote for ticker: {}", ticker);
        
        try {
            InstrumentResponse.Quote quote = instrumentService.getCurrentQuote(ticker);
            return ResponseEntity.ok(quote);
        } catch (IllegalArgumentException e) {
            log.warn("Instrument not found: {}", ticker);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            log.error("Error fetching quote for ticker: {}", ticker, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Get historical OHLCV price data for an instrument.
     * GET /api/instruments/{ticker}/history?period=1Y
     *
     * Path Parameters:
     * - ticker: Stock ticker symbol (e.g., "AAPL", "MSFT")
     *
     * Query Parameters:
     * - period (optional, default: "1Y"): Time period for historical data
     *   Valid values: 1D, 1W, 1M, 3M, 6M, 1Y, ALL
     *   Example: period=6M (last 6 months of data)
     *
     * Response: 200 OK
     * {
     *   "ticker": "AAPL",
     *   "period": "1Y",
     *   "barCount": 252,
     *   "bars": [
     *     {
     *       "timestamp": "2026-09-29T16:00:00Z",
     *       "open": 226.50,
     *       "high": 228.00,
     *       "low": 226.25,
     *       "close": 227.75,
     *       "volume": 45230000
     *     },
     *     ...
     *   ],
     *   "summary": {
     *     "highest": 235.80,
     *     "lowest": 195.40,
     *     "startPrice": 210.50,
     *     "endPrice": 227.75,
     *     "change": 17.25,
     *     "changePercent": 8.20,
     *     "avgVolume": 52340000
     *   }
     * }
     *
     * @param ticker Stock ticker symbol (case-insensitive, will be converted to uppercase)
     * @param period Time period: 1D, 1W, 1M, 3M, 6M, 1Y (default), or ALL
     * @return ResponseEntity with HistoryResponse (HTTP 200) or 404 if not found
     */
    @GetMapping("/{ticker}/history")
    @Operation(summary = "Get historical prices", description = "Returns OHLCV historical price data for an instrument and period.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historical prices returned"),
        @ApiResponse(responseCode = "400", description = "Invalid ticker or period")
    })
    public ResponseEntity<InstrumentResponse.HistoryResponse> getHistoricalPrices(
            @Parameter(description = "Instrument ticker symbol", example = "AAPL", required = true)
            @PathVariable("ticker") String ticker,
            @Parameter(description = "Historical range: 1D, 1W, 1M, 3M, 6M, 1Y, or ALL", example = "1Y")
            @RequestParam(value = "period", defaultValue = "1Y") String period) {
        log.debug("Get historical prices for ticker: {} with period: {}", ticker, period);
        
        try {
            InstrumentResponse.HistoryResponse history = instrumentService.getHistoricalPrices(ticker, period);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for ticker {} with period {}: {}", ticker, period, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
