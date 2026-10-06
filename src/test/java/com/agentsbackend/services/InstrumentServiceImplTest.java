package com.agentsbackend.services;

import com.agentsbackend.DTO.response.InstrumentResponse;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.InstrumentPriceHistory;
import com.agentsbackend.enums.AssetClass;
import com.agentsbackend.repos.InstrumentRepository;
import com.agentsbackend.repos.InstrumentPriceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Instrument Service Implementation Tests")
class InstrumentServiceImplTest {

    private InstrumentServiceImpl service;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private InstrumentPriceHistoryRepository priceHistoryRepository;

    private UUID instrumentId;
    private Instrument mockInstrument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new InstrumentServiceImpl(instrumentRepository, priceHistoryRepository);

        // Setup common test data
        instrumentId = UUID.randomUUID();
        mockInstrument = new Instrument();
        mockInstrument.setInstrumentId(instrumentId);
        mockInstrument.setTicker("TECH");
        mockInstrument.setName("Technology Stock");
        mockInstrument.setAssetClass(AssetClass.STOCK);
        mockInstrument.setIndustry("Technology");
        mockInstrument.setBid(new BigDecimal("249.50"));
        mockInstrument.setAsk(new BigDecimal("250.75"));
        mockInstrument.setPriceUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    // ============================================================
    // Search Instruments Tests
    // ============================================================

    @Test
    @DisplayName("searchInstruments - should return results for valid query")
    void searchInstruments_ValidQuery_ReturnsResults() {
        // Arrange
        String query = "tech";
        when(instrumentRepository.searchByQuery(query))
            .thenReturn(Arrays.asList(mockInstrument));

        // Act
        InstrumentResponse.SearchResultWrapper results = service.searchInstruments(query);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.getResults().size());
        assertEquals("TECH", results.getResults().get(0).getTicker());
        assertEquals("Technology Stock", results.getResults().get(0).getName());
        assertEquals("STOCK", results.getResults().get(0).getAssetClass());
        verify(instrumentRepository, times(1)).searchByQuery(query);
    }

    @Test
    @DisplayName("searchInstruments - should return empty results")
    void searchInstruments_NoMatches_ReturnsEmptyResults() {
        // Arrange
        String query = "xyz";
        when(instrumentRepository.searchByQuery(query))
            .thenReturn(Collections.emptyList());

        // Act
        InstrumentResponse.SearchResultWrapper results = service.searchInstruments(query);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.getResults().size());
    }

    @Test
    @DisplayName("searchInstruments - should throw for null query")
    void searchInstruments_NullQuery_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchInstruments(null);
        });
    }

    @Test
    @DisplayName("searchInstruments - should throw for empty query")
    void searchInstruments_EmptyQuery_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchInstruments("");
        });
    }

    @Test
    @DisplayName("searchInstruments - should throw for query exceeding 50 characters")
    void searchInstruments_QueryTooLong_ThrowsException() {
        // Arrange
        String query = "a".repeat(51);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchInstruments(query);
        });
    }

    @Test
    @DisplayName("searchInstruments - should trim whitespace")
    void searchInstruments_WithWhitespace_TrimmedAndReturnsResults() {
        // Arrange
        String query = "  tech  ";
        when(instrumentRepository.searchByQuery("tech"))
            .thenReturn(Arrays.asList(mockInstrument));

        // Act
        InstrumentResponse.SearchResultWrapper results = service.searchInstruments(query);

        // Assert
        assertEquals(1, results.getResults().size());
        verify(instrumentRepository, times(1)).searchByQuery("tech");
    }

    @Test
    @DisplayName("searchInstruments - should return multiple results")
    void searchInstruments_MultipleMatches_ReturnAll() {
        // Arrange
        String query = "a";
        Instrument inst1 = new Instrument();
        inst1.setTicker("AAPL");
        inst1.setName("Apple Inc.");
        inst1.setAssetClass(AssetClass.STOCK);
        inst1.setIndustry("Technology");

        Instrument inst2 = new Instrument();
        inst2.setTicker("AMZN");
        inst2.setName("Amazon.com Inc.");
        inst2.setAssetClass(AssetClass.STOCK);
        inst2.setIndustry("Technology");

        when(instrumentRepository.searchByQuery(query))
            .thenReturn(Arrays.asList(inst1, inst2));

        // Act
        InstrumentResponse.SearchResultWrapper results = service.searchInstruments(query);

        // Assert
        assertEquals(2, results.getResults().size());
    }

    // ============================================================
    // Get Instrument Details Tests
    // ============================================================

    @Test
    @DisplayName("getInstrumentDetails - should return details for valid ticker")
    void getInstrumentDetails_ValidTicker_ReturnsDetails() {
        // Arrange
        String ticker = "TECH";
        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));

        // Act
        InstrumentResponse.InstrumentDetail details = service.getInstrumentDetails(ticker);

        // Assert
        assertNotNull(details);
        assertEquals("TECH", details.getTicker());
        assertEquals("Technology Stock", details.getName());
        assertEquals("STOCK", details.getAssetClass());
        assertEquals("Technology", details.getIndustry());
        verify(instrumentRepository, times(1)).findByTicker("TECH");
    }

    @Test
    @DisplayName("getInstrumentDetails - should throw for invalid ticker")
    void getInstrumentDetails_InvalidTicker_ThrowsException() {
        // Arrange
        String ticker = "INVALID";
        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getInstrumentDetails(ticker);
        });
    }

    @Test
    @DisplayName("getInstrumentDetails - should be case-insensitive")
    void getInstrumentDetails_LowercaseTicker_ReturnsDetails() {
        // Arrange
        String ticker = "tech";
        when(instrumentRepository.findByTicker("TECH"))
            .thenReturn(Optional.of(mockInstrument));

        // Act
        InstrumentResponse.InstrumentDetail details = service.getInstrumentDetails(ticker);

        // Assert
        assertEquals("TECH", details.getTicker());
        verify(instrumentRepository, times(1)).findByTicker("TECH");
    }

    // ============================================================
    // Get Current Quote Tests
    // ============================================================

    @Test
    @DisplayName("getCurrentQuote - should return quote for valid ticker")
    void getCurrentQuote_ValidTicker_ReturnsQuote() {
        // Arrange
        String ticker = "TECH";
        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));

        // Act
        InstrumentResponse.Quote quote = service.getCurrentQuote(ticker);

        // Assert
        assertNotNull(quote);
        assertEquals("TECH", quote.getTicker());
        assertEquals(new BigDecimal("249.50"), quote.getBid());
        assertEquals(new BigDecimal("250.75"), quote.getAsk());
        assertEquals(new BigDecimal("250.75"), quote.getLastPrice());
        verify(instrumentRepository, times(1)).findByTicker("TECH");
    }

    @Test
    @DisplayName("getCurrentQuote - should throw for invalid ticker")
    void getCurrentQuote_InvalidTicker_ThrowsException() {
        // Arrange
        String ticker = "INVALID";
        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getCurrentQuote(ticker);
        });
    }

    @Test
    @DisplayName("getCurrentQuote - should set isStale to true for old data")
    void getCurrentQuote_StaleData_SetsStaleFlag() {
        // Arrange
        String ticker = "TECH";
        Instrument staleInstrument = new Instrument();
        staleInstrument.setTicker("TECH");
        staleInstrument.setAssetClass(AssetClass.STOCK);
        staleInstrument.setBid(new BigDecimal("249.50"));
        staleInstrument.setAsk(new BigDecimal("250.75"));
        staleInstrument.setPriceUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(staleInstrument));

        // Act
        InstrumentResponse.Quote quote = service.getCurrentQuote(ticker);

        // Assert
        assertTrue(quote.isStale());
    }

    @Test
    @DisplayName("getCurrentQuote - should set isStale to true for null timestamp")
    void getCurrentQuote_NullTimestamp_SetsStaleFlag() {
        // Arrange
        String ticker = "TECH";
        Instrument noTimestampInstrument = new Instrument();
        noTimestampInstrument.setTicker("TECH");
        noTimestampInstrument.setAssetClass(AssetClass.STOCK);
        noTimestampInstrument.setBid(new BigDecimal("249.50"));
        noTimestampInstrument.setAsk(new BigDecimal("250.75"));
        noTimestampInstrument.setPriceUpdatedAt(null);

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(noTimestampInstrument));

        // Act
        InstrumentResponse.Quote quote = service.getCurrentQuote(ticker);

        // Assert
        assertTrue(quote.isStale());
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

        InstrumentPriceHistory history = new InstrumentPriceHistory();
        history.setPriceHistoryId(UUID.randomUUID());
        history.setInstrumentId(instrumentId);
        history.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        history.setOpen(new BigDecimal("245.00"));
        history.setHigh(new BigDecimal("250.50"));
        history.setLow(new BigDecimal("244.00"));
        history.setClose(new BigDecimal("248.12"));
        history.setVolume(1500000L);

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Arrays.asList(history));

        // Act
        InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);

        // Assert
        assertNotNull(response);
        assertEquals("TECH", response.getTicker());
        assertEquals("1Y", response.getPeriod());
        assertEquals(1, response.getBarCount());
        assertNotNull(response.getSummary());
    }

    @Test
    @DisplayName("getHistoricalPrices - should return empty history")
    void getHistoricalPrices_NoData_ReturnsEmptyHistory() {
        // Arrange
        String ticker = "TECH";
        String period = "1D";

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(0, response.getBarCount());
        assertEquals(0, response.getBars().size());
    }

    @Test
    @DisplayName("getHistoricalPrices - should throw for invalid period")
    void getHistoricalPrices_InvalidPeriod_ThrowsException() {
        // Arrange
        String ticker = "TECH";
        String period = "INVALID";

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getHistoricalPrices(ticker, period);
        });
    }

    @Test
    @DisplayName("getHistoricalPrices - should support all valid periods")
    void getHistoricalPrices_AllValidPeriods_ReturnSuccess() {
        // Arrange
        String ticker = "TECH";
        String[] periods = {"1D", "1W", "1M", "3M", "6M", "1Y", "ALL"};

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        for (String period : periods) {
            InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);
            assertEquals(period, response.getPeriod());
        }
    }

    @Test
    @DisplayName("getHistoricalPrices - should use 1Y as default period")
    void getHistoricalPrices_NullPeriod_Uses1YDefault() {
        // Arrange
        String ticker = "TECH";
        String period = null;

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals("1Y", response.getPeriod());
    }

    @Test
    @DisplayName("getHistoricalPrices - should calculate summary statistics correctly")
    void getHistoricalPrices_MultipleBars_CalculatesSummaryCorrectly() {
        // Arrange
        String ticker = "TECH";
        String period = "1Y";

        InstrumentPriceHistory bar1 = new InstrumentPriceHistory();
        bar1.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2));
        bar1.setOpen(new BigDecimal("245.00"));
        bar1.setHigh(new BigDecimal("250.50"));
        bar1.setLow(new BigDecimal("244.00"));
        bar1.setClose(new BigDecimal("248.12"));
        bar1.setVolume(1500000L);

        InstrumentPriceHistory bar2 = new InstrumentPriceHistory();
        bar2.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC).minusHours(1));
        bar2.setOpen(new BigDecimal("248.00"));
        bar2.setHigh(new BigDecimal("252.00"));
        bar2.setLow(new BigDecimal("247.50"));
        bar2.setClose(new BigDecimal("250.75"));
        bar2.setVolume(1200000L);

        InstrumentPriceHistory bar3 = new InstrumentPriceHistory();
        bar3.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        bar3.setOpen(new BigDecimal("250.50"));
        bar3.setHigh(new BigDecimal("255.00"));
        bar3.setLow(new BigDecimal("250.00"));
        bar3.setClose(new BigDecimal("252.30"));
        bar3.setVolume(1000000L);

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Arrays.asList(bar3, bar2, bar1));

        // Act
        InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);

        // Assert
        assertEquals(3, response.getBarCount());
        assertEquals(new BigDecimal("255.00"), response.getSummary().getHighest());
        assertEquals(new BigDecimal("244.00"), response.getSummary().getLowest());
        assertEquals(new BigDecimal("245.00"), response.getSummary().getStartPrice());
        assertEquals(new BigDecimal("252.30"), response.getSummary().getEndPrice());
        assertEquals(new BigDecimal("7.30"), response.getSummary().getChange());
        assertEquals(1233333L, response.getSummary().getAvgVolume());
    }

    @Test
    @DisplayName("getHistoricalPrices - should throw for invalid ticker")
    void getHistoricalPrices_InvalidTicker_ThrowsException() {
        // Arrange
        String ticker = "INVALID";
        String period = "1Y";

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getHistoricalPrices(ticker, period);
        });
    }

    @Test
    @DisplayName("getHistoricalPrices - should handle null timestamps in bars")
    void getHistoricalPrices_NullTimestampInBar_HandlesGracefully() {
        // Arrange
        String ticker = "TECH";
        String period = "1Y";

        InstrumentPriceHistory history = new InstrumentPriceHistory();
        history.setTimestamp(null);
        history.setOpen(new BigDecimal("245.00"));
        history.setHigh(new BigDecimal("250.50"));
        history.setLow(new BigDecimal("244.00"));
        history.setClose(new BigDecimal("248.12"));
        history.setVolume(1500000L);

        when(instrumentRepository.findByTicker(ticker.toUpperCase()))
            .thenReturn(Optional.of(mockInstrument));
        when(priceHistoryRepository.findHistoricalPrices(eq(instrumentId), any(), any()))
            .thenReturn(Arrays.asList(history));

        // Act
        InstrumentResponse.HistoryResponse response = service.getHistoricalPrices(ticker, period);

        // Assert
        assertNotNull(response.getBars());
        assertNull(response.getBars().get(0).getTimestamp());
    }
}
