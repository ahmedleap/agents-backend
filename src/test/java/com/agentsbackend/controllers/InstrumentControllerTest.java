package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.InstrumentResponse;
import com.agentsbackend.services.InstrumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Instrument Controller Tests")
class InstrumentControllerTest {

    private InstrumentController controller;

    @Mock
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new InstrumentController(instrumentService);
    }

    // ============================================================
    // Search Instruments Tests
    // ============================================================

    @Test
    @DisplayName("searchInstruments - should return results for valid query")
    void searchInstruments_ValidQuery_ReturnsResults() {
        // Arrange
        String query = "tech";
        InstrumentResponse.SearchResult result = InstrumentResponse.SearchResult.builder()
            .ticker("TECH")
            .name("Technology Stock")
            .assetClass("STOCK")
            .industry("Technology")
            .build();

        InstrumentResponse.SearchResultWrapper wrapper = 
            new InstrumentResponse.SearchResultWrapper(Arrays.asList(result));

        when(instrumentService.searchInstruments(query)).thenReturn(wrapper);

        // Act
        ResponseEntity<InstrumentResponse.SearchResultWrapper> response = 
            controller.searchInstruments(query);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("TECH", response.getBody().getResults().get(0).getTicker());
        verify(instrumentService, times(1)).searchInstruments(query);
    }

    @Test
    @DisplayName("searchInstruments - should return empty results")
    void searchInstruments_NoMatches_ReturnsEmptyResults() {
        // Arrange
        String query = "xyz";
        InstrumentResponse.SearchResultWrapper wrapper = 
            new InstrumentResponse.SearchResultWrapper(Collections.emptyList());

        when(instrumentService.searchInstruments(query)).thenReturn(wrapper);

        // Act
        ResponseEntity<InstrumentResponse.SearchResultWrapper> response = 
            controller.searchInstruments(query);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getResults().size());
    }

    @Test
    @DisplayName("searchInstruments - should return 400 for invalid query")
    void searchInstruments_InvalidQuery_ReturnsBadRequest() {
        // Arrange
        String query = "";
        when(instrumentService.searchInstruments(query))
            .thenThrow(new IllegalArgumentException("Search query must be 1-50 characters"));

        // Act
        ResponseEntity<InstrumentResponse.SearchResultWrapper> response = 
            controller.searchInstruments(query);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("searchInstruments - should return multiple results")
    void searchInstruments_MultipleMatches_ReturnsMultipleResults() {
        // Arrange
        String query = "a";
        InstrumentResponse.SearchResult result1 = InstrumentResponse.SearchResult.builder()
            .ticker("AAPL").name("Apple Inc.").assetClass("STOCK").industry("Technology").build();
        InstrumentResponse.SearchResult result2 = InstrumentResponse.SearchResult.builder()
            .ticker("AMZN").name("Amazon.com Inc.").assetClass("STOCK").industry("Technology").build();

        InstrumentResponse.SearchResultWrapper wrapper = 
            new InstrumentResponse.SearchResultWrapper(Arrays.asList(result1, result2));

        when(instrumentService.searchInstruments(query)).thenReturn(wrapper);

        // Act
        ResponseEntity<InstrumentResponse.SearchResultWrapper> response = 
            controller.searchInstruments(query);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
    }

    // ============================================================
    // Get Instrument Details Tests
    // ============================================================

    @Test
    @DisplayName("getInstrumentDetails - should return details for valid ticker")
    void getInstrumentDetails_ValidTicker_ReturnsDetails() {
        // Arrange
        String ticker = "TECH";
        InstrumentResponse.InstrumentDetail details = 
            InstrumentResponse.InstrumentDetail.builder()
                .ticker("TECH")
                .name("Technology Stock")
                .assetClass("STOCK")
                .industry("Technology")
                .build();

        when(instrumentService.getInstrumentDetails(ticker)).thenReturn(details);

        // Act
        ResponseEntity<InstrumentResponse.InstrumentDetail> response = 
            controller.getInstrumentDetails(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TECH", response.getBody().getTicker());
        assertEquals("Technology Stock", response.getBody().getName());
        verify(instrumentService, times(1)).getInstrumentDetails(ticker);
    }

    @Test
    @DisplayName("getInstrumentDetails - should return 404 for invalid ticker")
    void getInstrumentDetails_InvalidTicker_ReturnsNotFound() {
        // Arrange
        String ticker = "INVALID";
        when(instrumentService.getInstrumentDetails(ticker))
            .thenThrow(new IllegalArgumentException("Instrument not found: " + ticker));

        // Act
        ResponseEntity<InstrumentResponse.InstrumentDetail> response = 
            controller.getInstrumentDetails(ticker);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getInstrumentDetails - should be case-insensitive")
    void getInstrumentDetails_LowercaseTicker_ReturnDetails() {
        // Arrange
        String ticker = "tech";
        InstrumentResponse.InstrumentDetail details = 
            InstrumentResponse.InstrumentDetail.builder()
                .ticker("TECH")
                .name("Technology Stock")
                .assetClass("STOCK")
                .industry("Technology")
                .build();

        when(instrumentService.getInstrumentDetails(ticker)).thenReturn(details);

        // Act
        ResponseEntity<InstrumentResponse.InstrumentDetail> response = 
            controller.getInstrumentDetails(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TECH", response.getBody().getTicker());
    }

    // ============================================================
    // Get Current Quote Tests
    // ============================================================

    @Test
    @DisplayName("getCurrentQuote - should return quote for valid ticker")
    void getCurrentQuote_ValidTicker_ReturnsQuote() {
        // Arrange
        String ticker = "TECH";
        InstrumentResponse.Quote quote = InstrumentResponse.Quote.builder()
            .ticker("TECH")
            .bid(new BigDecimal("249.50"))
            .ask(new BigDecimal("250.75"))
            .lastPrice(new BigDecimal("250.75"))
            .priceChange(BigDecimal.ZERO)
            .priceChangePercent(BigDecimal.ZERO)
            .volume(1000000L)
            .timestamp(LocalDateTime.now())
            .isStale(false)
            .build();

        when(instrumentService.getCurrentQuote(ticker)).thenReturn(quote);

        // Act
        ResponseEntity<InstrumentResponse.Quote> response = 
            controller.getCurrentQuote(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TECH", response.getBody().getTicker());
        assertEquals(new BigDecimal("249.50"), response.getBody().getBid());
        assertEquals(new BigDecimal("250.75"), response.getBody().getAsk());
        assertFalse(response.getBody().isStale());
        verify(instrumentService, times(1)).getCurrentQuote(ticker);
    }

    @Test
    @DisplayName("getCurrentQuote - should return 404 for invalid ticker")
    void getCurrentQuote_InvalidTicker_ReturnsNotFound() {
        // Arrange
        String ticker = "INVALID";
        when(instrumentService.getCurrentQuote(ticker))
            .thenThrow(new IllegalArgumentException("Instrument not found: " + ticker));

        // Act
        ResponseEntity<InstrumentResponse.Quote> response = 
            controller.getCurrentQuote(ticker);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getCurrentQuote - should return 503 for service error")
    void getCurrentQuote_ServiceError_ReturnsServiceUnavailable() {
        // Arrange
        String ticker = "TECH";
        when(instrumentService.getCurrentQuote(ticker))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        ResponseEntity<InstrumentResponse.Quote> response = 
            controller.getCurrentQuote(ticker);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("getCurrentQuote - should handle stale quotes")
    void getCurrentQuote_StaleQuote_ReturnsStaleFlag() {
        // Arrange
        String ticker = "TECH";
        InstrumentResponse.Quote quote = InstrumentResponse.Quote.builder()
            .ticker("TECH")
            .bid(new BigDecimal("249.50"))
            .ask(new BigDecimal("250.75"))
            .lastPrice(new BigDecimal("250.75"))
            .isStale(true)
            .build();

        when(instrumentService.getCurrentQuote(ticker)).thenReturn(quote);

        // Act
        ResponseEntity<InstrumentResponse.Quote> response = 
            controller.getCurrentQuote(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isStale());
    }

    // ============================================================
    // Get Historical Prices Tests
    // ============================================================

    @Test
    @DisplayName("getHistoricalPrices - should return history for valid ticker and period")
    void getHistoricalPrices_ValidTickerAndPeriod_ReturnsHistory() {
        // Arrange
        String ticker = "TECH";
        String period = "1Y";

        InstrumentResponse.HistoricalBar bar = InstrumentResponse.HistoricalBar.builder()
            .timestamp(LocalDateTime.now())
            .open(new BigDecimal("245.00"))
            .high(new BigDecimal("250.50"))
            .low(new BigDecimal("244.00"))
            .close(new BigDecimal("248.12"))
            .volume(1500000L)
            .build();

        InstrumentResponse.PeriodSummary summary = InstrumentResponse.PeriodSummary.builder()
            .highest(new BigDecimal("255.00"))
            .lowest(new BigDecimal("244.00"))
            .startPrice(new BigDecimal("245.00"))
            .endPrice(new BigDecimal("252.30"))
            .change(new BigDecimal("7.30"))
            .changePercent(new BigDecimal("2.98"))
            .avgVolume(1233333L)
            .build();

        InstrumentResponse.HistoryResponse history = InstrumentResponse.HistoryResponse.builder()
            .ticker("TECH")
            .period("1Y")
            .barCount(1)
            .bars(Arrays.asList(bar))
            .summary(summary)
            .build();

        when(instrumentService.getHistoricalPrices(ticker, period)).thenReturn(history);

        // Act
        ResponseEntity<InstrumentResponse.HistoryResponse> response = 
            controller.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TECH", response.getBody().getTicker());
        assertEquals("1Y", response.getBody().getPeriod());
        assertEquals(1, response.getBody().getBarCount());
        assertEquals(1, response.getBody().getBars().size());
        assertNotNull(response.getBody().getSummary());
        verify(instrumentService, times(1)).getHistoricalPrices(ticker, period);
    }

    @Test
    @DisplayName("getHistoricalPrices - should return empty history")
    void getHistoricalPrices_NoData_ReturnsEmptyHistory() {
        // Arrange
        String ticker = "TECH";
        String period = "1D";

        InstrumentResponse.HistoryResponse history = InstrumentResponse.HistoryResponse.builder()
            .ticker("TECH")
            .period("1D")
            .barCount(0)
            .bars(Collections.emptyList())
            .build();

        when(instrumentService.getHistoricalPrices(ticker, period)).thenReturn(history);

        // Act
        ResponseEntity<InstrumentResponse.HistoryResponse> response = 
            controller.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getBarCount());
    }

    @Test
    @DisplayName("getHistoricalPrices - should return 400 for invalid period")
    void getHistoricalPrices_InvalidPeriod_ReturnsBadRequest() {
        // Arrange
        String ticker = "TECH";
        String period = "INVALID";
        when(instrumentService.getHistoricalPrices(ticker, period))
            .thenThrow(new IllegalArgumentException("Invalid period: " + period));

        // Act
        ResponseEntity<InstrumentResponse.HistoryResponse> response = 
            controller.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("getHistoricalPrices - should support different periods")
    void getHistoricalPrices_DifferentPeriods_ReturnSuccess() {
        // Arrange
        String ticker = "TECH";
        String[] periods = {"1D", "1W", "1M", "3M", "6M", "1Y", "ALL"};

        for (String period : periods) {
            InstrumentResponse.HistoryResponse history = 
                InstrumentResponse.HistoryResponse.builder()
                    .ticker("TECH")
                    .period(period)
                    .barCount(0)
                    .bars(Collections.emptyList())
                    .build();

            when(instrumentService.getHistoricalPrices(ticker, period)).thenReturn(history);

            // Act
            ResponseEntity<InstrumentResponse.HistoryResponse> response = 
                controller.getHistoricalPrices(ticker, period);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(period, response.getBody().getPeriod());
        }
    }

    @Test
    @DisplayName("getHistoricalPrices - should handle default period")
    void getHistoricalPrices_DefaultPeriod_Uses1Y() {
        // Arrange
        String ticker = "TECH";
        String period = "1Y";

        InstrumentResponse.HistoryResponse history = InstrumentResponse.HistoryResponse.builder()
            .ticker("TECH")
            .period("1Y")
            .barCount(0)
            .bars(Collections.emptyList())
            .build();

        when(instrumentService.getHistoricalPrices(ticker, period)).thenReturn(history);

        // Act
        ResponseEntity<InstrumentResponse.HistoryResponse> response = 
            controller.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1Y", response.getBody().getPeriod());
    }
}
