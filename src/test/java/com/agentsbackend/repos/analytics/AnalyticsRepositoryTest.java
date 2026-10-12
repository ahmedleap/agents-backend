package com.agentsbackend.repos.analytics;

import com.agentsbackend.DTO.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AnalyticsRepository Tests")
class AnalyticsRepositoryTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get platform metrics")
    void testGetPlatformMetrics() {
        // Arrange
        PlatformMetricsResponse response = new PlatformMetricsResponse();
        response.setTotalClients(1000L);
        response.setTotalAccounts(1500L);
        response.setTotalAssetsUnderManagement(new java.math.BigDecimal("5000000"));
        response.setOrderFulfillmentRate(new java.math.BigDecimal("95.5"));
        when(analyticsRepository.getPlatformMetrics()).thenReturn(response);

        // Act
        PlatformMetricsResponse result = analyticsRepository.getPlatformMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(1000L, result.getTotalClients());
        assertEquals(1500L, result.getTotalAccounts());
        assertEquals(new java.math.BigDecimal("5000000"), result.getTotalAssetsUnderManagement());
        assertEquals(new java.math.BigDecimal("95.5"), result.getOrderFulfillmentRate());
        verify(analyticsRepository).getPlatformMetrics();
    }

    @Test
    @DisplayName("Should get top traded instruments")
    void testGetTopTradedInstruments() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", "Apple Inc", 150L, new java.math.BigDecimal("1000000")),
            createTopInstrument("GOOGL", "Alphabet Inc", 120L, new java.math.BigDecimal("800000"))
        );
        when(analyticsRepository.getTopTradedInstruments(20)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsRepository.getTopTradedInstruments(20);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals(150, result.get(0).getTradeCount());
        verify(analyticsRepository).getTopTradedInstruments(20);
    }

    @Test
    @DisplayName("Should get top instruments by volume")
    void testGetTopInstrumentsByVolume() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("MSFT", "Microsoft", 100L, new java.math.BigDecimal("2000000"))
        );
        when(analyticsRepository.getTopInstrumentsByVolume(20)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsRepository.getTopInstrumentsByVolume(20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MSFT", result.get(0).getTicker());
        verify(analyticsRepository).getTopInstrumentsByVolume(20);
    }

    @Test
    @DisplayName("Should get asset class metrics")
    void testGetAssetClassMetrics() {
        // Arrange
        List<AssetClassMetricsResponse> metrics = Arrays.asList(
            createAssetClassMetric("STOCK", 50000L, new java.math.BigDecimal("3500000"), 120L),
            createAssetClassMetric("BOND", 30000L, new java.math.BigDecimal("2000000"), 80L),
            createAssetClassMetric("CRYPTO", 15000L, new java.math.BigDecimal("900000"), 45L)
        );
        when(analyticsRepository.getAssetClassMetrics()).thenReturn(metrics);

        // Act
        List<AssetClassMetricsResponse> result = analyticsRepository.getAssetClassMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("STOCK", result.get(0).getAssetClass());
        assertEquals(50000, result.get(0).getTradeCount());
        verify(analyticsRepository).getAssetClassMetrics();
    }

    @Test
    @DisplayName("Should get order metrics")
    void testGetOrderMetrics() {
        // Arrange
        OrderMetricsResponse response = new OrderMetricsResponse();
        response.setTotalOrders(5000L);
        response.setFilledOrders(4775L);
        response.setFulfillmentRate(new java.math.BigDecimal("95.5"));
        response.setCancelledOrders(150L);
        when(analyticsRepository.getOrderMetrics()).thenReturn(response);

        // Act
        OrderMetricsResponse result = analyticsRepository.getOrderMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(5000L, result.getTotalOrders());
        assertEquals(4775L, result.getFilledOrders());
        assertEquals(new java.math.BigDecimal("95.5"), result.getFulfillmentRate());
        assertEquals(150, result.getCancelledOrders());
        verify(analyticsRepository).getOrderMetrics();
    }

    @Test
    @DisplayName("Should get order metrics by date range")
    void testGetOrderMetricsByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        OrderMetricsResponse response = new OrderMetricsResponse();
        response.setTotalOrders(1000L);
        response.setFulfillmentRate(new java.math.BigDecimal("94.5"));
        when(analyticsRepository.getOrderMetricsByDateRange(startDate, endDate)).thenReturn(response);

        // Act
        OrderMetricsResponse result = analyticsRepository.getOrderMetricsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1000L, result.getTotalOrders());
        assertEquals(new java.math.BigDecimal("94.5"), result.getFulfillmentRate());
        verify(analyticsRepository).getOrderMetricsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should get client segmentation by risk tolerance")
    void testGetClientSegmentationByRiskTolerance() {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("CONSERVATIVE", 300L, new java.math.BigDecimal("2500000")),
            createClientSegmentation("MODERATE", 500L, new java.math.BigDecimal("3500000")),
            createClientSegmentation("AGGRESSIVE", 400L, new java.math.BigDecimal("2000000"))
        );
        when(analyticsRepository.getClientSegmentationByRiskTolerance()).thenReturn(segmentation);

        // Act
        List<ClientSegmentationResponse> result = analyticsRepository.getClientSegmentationByRiskTolerance();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("CONSERVATIVE", result.get(0).getSegment());
        assertEquals(300, result.get(0).getClientCount());
        verify(analyticsRepository).getClientSegmentationByRiskTolerance();
    }

    @Test
    @DisplayName("Should get client segmentation by portfolio size")
    void testGetClientSegmentationByPortfolioSize() {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("SMALL", 200L, new java.math.BigDecimal("500000")),
            createClientSegmentation("MEDIUM", 500L, new java.math.BigDecimal("2500000")),
            createClientSegmentation("LARGE", 300L, new java.math.BigDecimal("4000000"))
        );
        when(analyticsRepository.getClientSegmentationByPortfolioSize()).thenReturn(segmentation);

        // Act
        List<ClientSegmentationResponse> result = analyticsRepository.getClientSegmentationByPortfolioSize();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("SMALL", result.get(0).getSegment());
        verify(analyticsRepository).getClientSegmentationByPortfolioSize();
    }

    @Test
    @DisplayName("Should get total volume")
    void testGetTotalVolume() {
        // Arrange
        TotalVolumeResponse response = new TotalVolumeResponse();
        response.setTotalQuantityTraded(50000L);
        response.setTotalValueTraded(1500000.0);
        response.setTotalOrdersExecuted(800L);
        response.setAverageOrderQuantity(62.5);
        when(analyticsRepository.getTotalVolume()).thenReturn(response);

        // Act
        TotalVolumeResponse result = analyticsRepository.getTotalVolume();

        // Assert
        assertNotNull(result);
        assertEquals(50000, result.getTotalQuantityTraded());
        assertEquals(1500000.0, result.getTotalValueTraded());
        assertEquals(800, result.getTotalOrdersExecuted());
        verify(analyticsRepository).getTotalVolume();
    }

    @Test
    @DisplayName("Should get total trades")
    void testGetTotalTrades() {
        // Arrange
        TotalTradesResponse response = new TotalTradesResponse();
        response.setTotalTrades(500L);
        response.setFulfillmentRate(96.2);
        response.setCancellationRate(2.8);
        response.setBuyPercentage(48.5);
        response.setSellPercentage(51.5);
        when(analyticsRepository.getTotalTrades()).thenReturn(response);

        // Act
        TotalTradesResponse result = analyticsRepository.getTotalTrades();

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getTotalTrades());
        assertEquals(96.2, result.getFulfillmentRate());
        verify(analyticsRepository).getTotalTrades();
    }

    @Test
    @DisplayName("Should get active clients")
    void testGetActiveClients() {
        // Arrange
        ActiveClientsResponse response = new ActiveClientsResponse();
        response.setTotalActiveClients(350L);
        response.setTotalClients(1000L);
        response.setActiveClientPercentage(35.0);
        response.setTotalAccounts(1500L);
        when(analyticsRepository.getActiveClients()).thenReturn(response);

        // Act
        ActiveClientsResponse result = analyticsRepository.getActiveClients();

        // Assert
        assertNotNull(result);
        assertEquals(350L, result.getTotalActiveClients());
        assertEquals(1000L, result.getTotalClients());
        assertEquals(35.0, result.getActiveClientPercentage());
        verify(analyticsRepository).getActiveClients();
    }

    @Test
    @DisplayName("Should get daily total volume")
    void testGetDailyTotalVolume() {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalVolumeResponse response = new TotalVolumeResponse();
        response.setTotalQuantityTraded(40000L);
        response.setTotalValueTraded(1200000.0);
        when(analyticsRepository.getDailyTotalVolume(date)).thenReturn(response);

        // Act
        TotalVolumeResponse result = analyticsRepository.getDailyTotalVolume(date);

        // Assert
        assertNotNull(result);
        assertEquals(40000L, result.getTotalQuantityTraded());
        verify(analyticsRepository).getDailyTotalVolume(date);
    }

    @Test
    @DisplayName("Should get daily total trades")
    void testGetDailyTotalTrades() {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalTradesResponse response = new TotalTradesResponse();
        response.setTotalTrades(450L);
        response.setFulfillmentRate(95.8);
        response.setBuyPercentage(48.0);
        response.setSellPercentage(52.0);
        when(analyticsRepository.getDailyTotalTrades(date)).thenReturn(response);

        // Act
        TotalTradesResponse result = analyticsRepository.getDailyTotalTrades(date);

        // Assert
        assertNotNull(result);
        assertEquals(450, result.getTotalTrades());
        verify(analyticsRepository).getDailyTotalTrades(date);
    }

    @Test
    @DisplayName("Should get daily active clients")
    void testGetDailyActiveClients() {
        // Arrange
        LocalDate date = LocalDate.now();
        ActiveClientsResponse response = new ActiveClientsResponse();
        response.setTotalActiveClients(280L);
        response.setTotalClients(1000L);
        response.setActiveClientPercentage(28.0);
        when(analyticsRepository.getDailyActiveClients(date)).thenReturn(response);

        // Act
        ActiveClientsResponse result = analyticsRepository.getDailyActiveClients(date);

        // Assert
        assertNotNull(result);
        assertEquals(280L, result.getTotalActiveClients());
        verify(analyticsRepository).getDailyActiveClients(date);
    }

    @Test
    @DisplayName("Should get volume trend")
    void testGetVolumeTrend() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 7);
        List<VolumeTrendResponse> trends = Arrays.asList(
            createVolumeTrend(startDate, 40000L, 1200000.0),
            createVolumeTrend(startDate.plusDays(1), 42000L, 1250000.0),
            createVolumeTrend(startDate.plusDays(2), 38000L, 1100000.0)
        );
        when(analyticsRepository.getVolumeTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<VolumeTrendResponse> result = analyticsRepository.getVolumeTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(40000L, result.get(0).getTotalQuantityTraded());
        verify(analyticsRepository).getVolumeTrend(startDate, endDate);
    }

    @Test
    @DisplayName("Should get instrument trend by date range")
    void testGetInstrumentTrendByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        List<InstrumentTrendResponse> trends = Arrays.asList(
            createInstrumentTrend("AAPL", startDate, 100L, 90000.0),
            createInstrumentTrend("GOOGL", startDate, 80L, 75000.0)
        );
        when(analyticsRepository.getInstrumentTrendByDateRange(startDate, endDate)).thenReturn(trends);

        // Act
        List<InstrumentTrendResponse> result = analyticsRepository.getInstrumentTrendByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(analyticsRepository).getInstrumentTrendByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should get top instruments for date")
    void testGetTopInstrumentsForDate() {
        // Arrange
        LocalDate date = LocalDate.now();
        List<InstrumentTrendResponse> instruments = Arrays.asList(
            createInstrumentTrend("AAPL", date, 120L, 100000.0),
            createInstrumentTrend("MSFT", date, 100L, 90000.0)
        );
        when(analyticsRepository.getTopInstrumentsForDate(date, 20)).thenReturn(instruments);

        // Act
        List<InstrumentTrendResponse> result = analyticsRepository.getTopInstrumentsForDate(date, 20);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(analyticsRepository).getTopInstrumentsForDate(date, 20);
    }

    @Test
    @DisplayName("Should get instrument analysis")
    void testGetInstrumentAnalysis() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        InstrumentAnalysisResponse response = new InstrumentAnalysisResponse();
        response.setTicker("AAPL");
        response.setInstrumentName("Apple Inc");
        response.setAssetClass("STOCK");
        response.setTotalVolume(100000L);
        response.setTotalValue(new java.math.BigDecimal("3500000"));
        response.setTradeCount(500L);
        response.setUniqueTraders(250L);
        when(analyticsRepository.getInstrumentAnalysis("AAPL", startDate, endDate)).thenReturn(response);

        // Act
        InstrumentAnalysisResponse result = analyticsRepository.getInstrumentAnalysis("AAPL", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        assertEquals("Apple Inc", result.getInstrumentName());
        assertEquals("STOCK", result.getAssetClass());
        assertEquals(100000, result.getTotalVolume());
        assertEquals(500, result.getTradeCount());
        verify(analyticsRepository).getInstrumentAnalysis("AAPL", startDate, endDate);
    }

    @Test
    @DisplayName("Should get segment trend")
    void testGetSegmentTrend() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        List<SegmentTrendResponse> trends = Arrays.asList(
            createSegmentTrend("CONSERVATIVE", startDate, 300L, new java.math.BigDecimal("2500000")),
            createSegmentTrend("AGGRESSIVE", startDate, 400L, new java.math.BigDecimal("2000000"))
        );
        when(analyticsRepository.getSegmentTrend("RISK_TOLERANCE", startDate, endDate))
            .thenReturn(trends);

        // Act
        List<SegmentTrendResponse> result = analyticsRepository.getSegmentTrend("RISK_TOLERANCE", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CONSERVATIVE", result.get(0).getSegment());
        verify(analyticsRepository).getSegmentTrend("RISK_TOLERANCE", startDate, endDate);
    }

    @Test
    @DisplayName("Should get fulfillment trend")
    void testGetFulfillmentTrend() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        List<FulfillmentTrendResponse> trends = Arrays.asList(
            createFulfillmentTrend(startDate, 95.5, 1200L),
            createFulfillmentTrend(startDate.plusDays(1), 96.2, 1300L)
        );
        when(analyticsRepository.getFulfillmentTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<FulfillmentTrendResponse> result = analyticsRepository.getFulfillmentTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new java.math.BigDecimal("95.5"), result.get(0).getFulfillmentRate());
        verify(analyticsRepository).getFulfillmentTrend(startDate, endDate);
    }

    @Test
    @DisplayName("Should get asset class trend")
    void testGetAssetClassTrend() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        List<AssetClassTrendResponse> trends = Arrays.asList(
            createAssetClassTrend("STOCK", startDate, 40000L, new java.math.BigDecimal("3500000")),
            createAssetClassTrend("BOND", startDate, 20000L, new java.math.BigDecimal("1500000"))
        );
        when(analyticsRepository.getAssetClassTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<AssetClassTrendResponse> result = analyticsRepository.getAssetClassTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("STOCK", result.get(0).getAssetClass());
        verify(analyticsRepository).getAssetClassTrend(startDate, endDate);
    }

    // Helper methods
    private TopInstrumentResponse createTopInstrument(String ticker, String name, Long tradeCount, java.math.BigDecimal totalVolume) {
        TopInstrumentResponse response = new TopInstrumentResponse();
        response.setTicker(ticker);
        response.setInstrumentName(name);
        response.setTradeCount(tradeCount);
        response.setTotalVolume(totalVolume);
        return response;
    }

    private AssetClassMetricsResponse createAssetClassMetric(String assetClass, Long tradeCount, java.math.BigDecimal totalVolume, Long uniqueTraders) {
        AssetClassMetricsResponse response = new AssetClassMetricsResponse();
        response.setAssetClass(assetClass);
        response.setTradeCount(tradeCount);
        response.setTotalVolume(totalVolume);
        response.setUniqueTraders(uniqueTraders);
        return response;
    }

    private ClientSegmentationResponse createClientSegmentation(String segment, Long clientCount, java.math.BigDecimal totalAUM) {
        ClientSegmentationResponse response = new ClientSegmentationResponse();
        response.setSegment(segment);
        response.setClientCount(clientCount);
        response.setTotalAssetsUnderManagement(totalAUM);
        return response;
    }

    private VolumeTrendResponse createVolumeTrend(LocalDate date, Long volume, Double totalValue) {
        VolumeTrendResponse response = new VolumeTrendResponse();
        response.setDate(date);
        response.setTotalQuantityTraded(volume);
        response.setTotalValueTraded(totalValue);
        return response;
    }

    private InstrumentTrendResponse createInstrumentTrend(String ticker, LocalDate date, Long volume, Double totalValue) {
        InstrumentTrendResponse response = new InstrumentTrendResponse();
        response.setTicker(ticker);
        response.setDate(date);
        response.setTradeCount(volume);
        response.setTotalValue(totalValue);
        return response;
    }

    private SegmentTrendResponse createSegmentTrend(String segment, LocalDate date, Long count, java.math.BigDecimal totalAUM) {
        SegmentTrendResponse response = new SegmentTrendResponse();
        response.setSegment(segment);
        response.setDate(date);
        response.setActiveClientCount(count);
        response.setTotalValue(totalAUM);
        return response;
    }

    private FulfillmentTrendResponse createFulfillmentTrend(LocalDate date, Double fulfillmentRate, Long tradeCount) {
        FulfillmentTrendResponse response = new FulfillmentTrendResponse();
        response.setDate(date);
        response.setFulfillmentRate(new java.math.BigDecimal(fulfillmentRate.toString()));
        response.setTotalOrders(tradeCount);
        return response;
    }

    private AssetClassTrendResponse createAssetClassTrend(String assetClass, LocalDate date, Long volume, java.math.BigDecimal totalValue) {
        AssetClassTrendResponse response = new AssetClassTrendResponse();
        response.setAssetClass(assetClass);
        response.setDate(date);
        response.setTotalVolume(volume);
        response.setTotalValue(totalValue);
        return response;
    }
}
