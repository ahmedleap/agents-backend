package com.agentsbackend.services;

import com.agentsbackend.repos.analytics.AnalyticsRepository;
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

@DisplayName("AnalyticsServiceImpl Tests")
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyticsService = new AnalyticsServiceImpl(analyticsRepository);
    }

    @Test
    @DisplayName("Should get platform metrics")
    void testGetPlatformMetrics() {
        // Arrange
        PlatformMetricsResponse response = new PlatformMetricsResponse();
        response.setTotalClients(1000L);
        response.setTotalAccounts(1500L);
        when(analyticsRepository.getPlatformMetrics()).thenReturn(response);

        // Act
        PlatformMetricsResponse result = analyticsService.getPlatformMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.getTotalClients());
        assertEquals(1500, result.getTotalAccounts());
        verify(analyticsRepository).getPlatformMetrics();
    }

    @Test
    @DisplayName("Should get top traded instruments with valid limit")
    void testGetTopTradedInstruments() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", 150),
            createTopInstrument("GOOGL", 120)
        );
        when(analyticsRepository.getTopTradedInstruments(20)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsService.getTopTradedInstruments(20);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(analyticsRepository).getTopTradedInstruments(20);
    }

    @Test
    @DisplayName("Should use default limit when null")
    void testGetTopTradedInstrumentsWithNullLimit() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", 150)
        );
        when(analyticsRepository.getTopTradedInstruments(20)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsService.getTopTradedInstruments(null);

        // Assert
        assertNotNull(result);
        verify(analyticsRepository).getTopTradedInstruments(20);
    }

    @Test
    @DisplayName("Should use default limit when limit is zero")
    void testGetTopTradedInstrumentsWithZeroLimit() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", 150)
        );
        when(analyticsRepository.getTopTradedInstruments(20)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsService.getTopTradedInstruments(0);

        // Assert
        assertNotNull(result);
        verify(analyticsRepository).getTopTradedInstruments(20);
    }

    @Test
    @DisplayName("Should get top instruments by volume")
    void testGetTopInstrumentsByVolume() {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", 150)
        );
        when(analyticsRepository.getTopInstrumentsByVolume(15)).thenReturn(instruments);

        // Act
        List<TopInstrumentResponse> result = analyticsService.getTopInstrumentsByVolume(15);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(analyticsRepository).getTopInstrumentsByVolume(15);
    }

    @Test
    @DisplayName("Should get asset class metrics")
    void testGetAssetClassMetrics() {
        // Arrange
        List<AssetClassMetricsResponse> metrics = Arrays.asList(
            createAssetClassMetric("STOCK", 50000),
            createAssetClassMetric("BOND", 30000)
        );
        when(analyticsRepository.getAssetClassMetrics()).thenReturn(metrics);

        // Act
        List<AssetClassMetricsResponse> result = analyticsService.getAssetClassMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("STOCK", result.get(0).getAssetClass());
        verify(analyticsRepository).getAssetClassMetrics();
    }

    @Test
    @DisplayName("Should get order metrics")
    void testGetOrderMetrics() {
        // Arrange
        OrderMetricsResponse response = new OrderMetricsResponse();
        response.setTotalOrders(5000L);
        response.setFulfillmentRate(new java.math.BigDecimal("95.5"));
        when(analyticsRepository.getOrderMetrics()).thenReturn(response);

        // Act
        OrderMetricsResponse result = analyticsService.getOrderMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(5000, result.getTotalOrders());
        assertEquals(new java.math.BigDecimal("95.5"), result.getFulfillmentRate());
        verify(analyticsRepository).getOrderMetrics();
    }

    @Test
    @DisplayName("Should get order metrics by date range")
    void testGetOrderMetricsByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        OrderMetricsResponse response = new OrderMetricsResponse();
        response.setTotalOrders(1000L);
        when(analyticsRepository.getOrderMetricsByDateRange(startDate, endDate)).thenReturn(response);

        // Act
        OrderMetricsResponse result = analyticsService.getOrderMetricsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.getTotalOrders());
        verify(analyticsRepository).getOrderMetricsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void testGetOrderMetricsByDateRangeInvalidDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getOrderMetricsByDateRange(startDate, endDate));
    }

    @Test
    @DisplayName("Should get client segmentation by risk tolerance")
    void testGetClientSegmentationByRiskTolerance() {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("CONSERVATIVE", 300),
            createClientSegmentation("AGGRESSIVE", 400)
        );
        when(analyticsRepository.getClientSegmentationByRiskTolerance()).thenReturn(segmentation);

        // Act
        List<ClientSegmentationResponse> result = analyticsService.getClientSegmentationByRiskTolerance();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getClientSegmentationByRiskTolerance();
    }

    @Test
    @DisplayName("Should get client segmentation by portfolio size")
    void testGetClientSegmentationByPortfolioSize() {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("SMALL", 200)
        );
        when(analyticsRepository.getClientSegmentationByPortfolioSize()).thenReturn(segmentation);

        // Act
        List<ClientSegmentationResponse> result = analyticsService.getClientSegmentationByPortfolioSize();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(analyticsRepository).getClientSegmentationByPortfolioSize();
    }

    @Test
    @DisplayName("Should get total volume")
    void testGetTotalVolume() {
        // Arrange
        TotalVolumeResponse response = new TotalVolumeResponse();
        response.setTotalQuantityTraded(50000L);
        response.setTotalValueTraded(1500000.0);
        when(analyticsRepository.getTotalVolume()).thenReturn(response);

        // Act
        TotalVolumeResponse result = analyticsService.getTotalVolume();

        // Assert
        assertNotNull(result);
        assertEquals(50000, result.getTotalQuantityTraded());
        verify(analyticsRepository).getTotalVolume();
    }

    @Test
    @DisplayName("Should get total trades")
    void testGetTotalTrades() {
        // Arrange
        TotalTradesResponse response = new TotalTradesResponse();
        response.setTotalTrades(500L);
        response.setFulfillmentRate(96.2);
        when(analyticsRepository.getTotalTrades()).thenReturn(response);

        // Act
        TotalTradesResponse result = analyticsService.getTotalTrades();

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getTotalTrades());
        verify(analyticsRepository).getTotalTrades();
    }

    @Test
    @DisplayName("Should get active clients")
    void testGetActiveClients() {
        // Arrange
        ActiveClientsResponse response = new ActiveClientsResponse();
        response.setTotalActiveClients(350L);
        response.setTotalClients(1000L);
        when(analyticsRepository.getActiveClients()).thenReturn(response);

        // Act
        ActiveClientsResponse result = analyticsService.getActiveClients();

        // Assert
        assertNotNull(result);
        assertEquals(350L, result.getTotalActiveClients());
        verify(analyticsRepository).getActiveClients();
    }

    @Test
    @DisplayName("Should get daily total volume")
    void testGetDailyTotalVolume() {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalVolumeResponse response = new TotalVolumeResponse();
        response.setTotalQuantityTraded(40000L);
        when(analyticsRepository.getDailyTotalVolume(date)).thenReturn(response);

        // Act
        TotalVolumeResponse result = analyticsService.getDailyTotalVolume(date);

        // Assert
        assertNotNull(result);
        assertEquals(40000, result.getTotalQuantityTraded());
        verify(analyticsRepository).getDailyTotalVolume(date);
    }

    @Test
    @DisplayName("Should get daily total trades")
    void testGetDailyTotalTrades() {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalTradesResponse response = new TotalTradesResponse();
        response.setTotalTrades(450L);
        when(analyticsRepository.getDailyTotalTrades(date)).thenReturn(response);

        // Act
        TotalTradesResponse result = analyticsService.getDailyTotalTrades(date);

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
        when(analyticsRepository.getDailyActiveClients(date)).thenReturn(response);

        // Act
        ActiveClientsResponse result = analyticsService.getDailyActiveClients(date);

        // Assert
        assertNotNull(result);
        assertEquals(280L, result.getTotalActiveClients());
        verify(analyticsRepository).getDailyActiveClients(date);
    }

    @Test
    @DisplayName("Should get volume trend")
    void testGetVolumeTrend() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<VolumeTrendResponse> trends = Arrays.asList(
            createVolumeTrend(startDate, 40000),
            createVolumeTrend(startDate.plusDays(1), 42000)
        );
        when(analyticsRepository.getVolumeTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<VolumeTrendResponse> result = analyticsService.getVolumeTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getVolumeTrend(startDate, endDate);
    }

    @Test
    @DisplayName("Should throw exception for invalid volume trend date range")
    void testGetVolumeTrendInvalidDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getVolumeTrend(startDate, endDate));
    }

    @Test
    @DisplayName("Should get instrument trend by date range")
    void testGetInstrumentTrendByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<InstrumentTrendResponse> trends = Arrays.asList(
            createInstrumentTrend("AAPL", startDate, 100),
            createInstrumentTrend("GOOGL", startDate, 80)
        );
        when(analyticsRepository.getInstrumentTrendByDateRange(startDate, endDate)).thenReturn(trends);

        // Act
        List<InstrumentTrendResponse> result = analyticsService.getInstrumentTrendByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getInstrumentTrendByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should get top instruments for date")
    void testGetTopInstrumentsForDate() {
        // Arrange
        LocalDate date = LocalDate.now();
        List<InstrumentTrendResponse> instruments = Arrays.asList(
            createInstrumentTrend("AAPL", date, 120),
            createInstrumentTrend("MSFT", date, 100)
        );
        when(analyticsRepository.getTopInstrumentsForDate(date, 20)).thenReturn(instruments);

        // Act
        List<InstrumentTrendResponse> result = analyticsService.getTopInstrumentsForDate(date, 20);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getTopInstrumentsForDate(date, 20);
    }

    @Test
    @DisplayName("Should use default limit for top instruments when null")
    void testGetTopInstrumentsForDateWithNullLimit() {
        // Arrange
        LocalDate date = LocalDate.now();
        List<InstrumentTrendResponse> instruments = Arrays.asList(
            createInstrumentTrend("AAPL", date, 120)
        );
        when(analyticsRepository.getTopInstrumentsForDate(date, 20)).thenReturn(instruments);

        // Act
        List<InstrumentTrendResponse> result = analyticsService.getTopInstrumentsForDate(date, null);

        // Assert
        assertNotNull(result);
        verify(analyticsRepository).getTopInstrumentsForDate(date, 20);
    }

    @Test
    @DisplayName("Should get instrument analysis")
    void testGetInstrumentAnalysis() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        InstrumentAnalysisResponse response = new InstrumentAnalysisResponse();
        response.setTicker("AAPL");
        response.setTotalVolume(100000L);
        response.setTradeCount(500L);
        when(analyticsRepository.getInstrumentAnalysis("AAPL", startDate, endDate)).thenReturn(response);

        // Act
        InstrumentAnalysisResponse result = analyticsService.getInstrumentAnalysis("AAPL", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        assertEquals(100000, result.getTotalVolume());
        verify(analyticsRepository).getInstrumentAnalysis("AAPL", startDate, endDate);
    }

    @Test
    @DisplayName("Should throw exception for null ticker in instrument analysis")
    void testGetInstrumentAnalysisWithNullTicker() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getInstrumentAnalysis(null, startDate, endDate));
    }

    @Test
    @DisplayName("Should throw exception for empty ticker in instrument analysis")
    void testGetInstrumentAnalysisWithEmptyTicker() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getInstrumentAnalysis("", startDate, endDate));
    }

    @Test
    @DisplayName("Should get segment trend")
    void testGetSegmentTrend() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<SegmentTrendResponse> trends = Arrays.asList(
            createSegmentTrend("CONSERVATIVE", startDate, 300),
            createSegmentTrend("AGGRESSIVE", startDate, 400)
        );
        when(analyticsRepository.getSegmentTrend("RISK_TOLERANCE", startDate, endDate))
            .thenReturn(trends);

        // Act
        List<SegmentTrendResponse> result = analyticsService.getSegmentTrend("RISK_TOLERANCE", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getSegmentTrend("RISK_TOLERANCE", startDate, endDate);
    }

    @Test
    @DisplayName("Should throw exception for invalid segment type")
    void testGetSegmentTrendInvalidSegmentType() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getSegmentTrend("INVALID_TYPE", startDate, endDate));
    }

    @Test
    @DisplayName("Should throw exception for null segment type")
    void testGetSegmentTrendNullSegmentType() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> analyticsService.getSegmentTrend(null, startDate, endDate));
    }

    @Test
    @DisplayName("Should get fulfillment trend")
    void testGetFulfillmentTrend() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<FulfillmentTrendResponse> trends = Arrays.asList(
            createFulfillmentTrend(startDate, 95.5),
            createFulfillmentTrend(startDate.plusDays(1), 96.2)
        );
        when(analyticsRepository.getFulfillmentTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<FulfillmentTrendResponse> result = analyticsService.getFulfillmentTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getFulfillmentTrend(startDate, endDate);
    }

    @Test
    @DisplayName("Should get asset class trend")
    void testGetAssetClassTrend() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<AssetClassTrendResponse> trends = Arrays.asList(
            createAssetClassTrend("STOCK", startDate, 40000),
            createAssetClassTrend("BOND", startDate, 20000)
        );
        when(analyticsRepository.getAssetClassTrend(startDate, endDate)).thenReturn(trends);

        // Act
        List<AssetClassTrendResponse> result = analyticsService.getAssetClassTrend(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(analyticsRepository).getAssetClassTrend(startDate, endDate);
    }

    // Helper methods
    private TopInstrumentResponse createTopInstrument(String ticker, Integer tradeCount) {
        TopInstrumentResponse response = new TopInstrumentResponse();
        response.setTicker(ticker);
        response.setTradeCount(Long.valueOf(tradeCount));
        return response;
    }

    private AssetClassMetricsResponse createAssetClassMetric(String assetClass, Integer tradeCount) {
        AssetClassMetricsResponse response = new AssetClassMetricsResponse();
        response.setAssetClass(assetClass);
        response.setTradeCount(Long.valueOf(tradeCount));
        return response;
    }

    private ClientSegmentationResponse createClientSegmentation(String segment, Integer clientCount) {
        ClientSegmentationResponse response = new ClientSegmentationResponse();
        response.setSegment(segment);
        response.setClientCount(Long.valueOf(clientCount));
        return response;
    }

    private VolumeTrendResponse createVolumeTrend(LocalDate date, Integer volume) {
        VolumeTrendResponse response = new VolumeTrendResponse();
        response.setDate(date);
        response.setTotalQuantityTraded(Long.valueOf(volume));
        return response;
    }

    private InstrumentTrendResponse createInstrumentTrend(String ticker, LocalDate date, Integer volume) {
        InstrumentTrendResponse response = new InstrumentTrendResponse();
        response.setTicker(ticker);
        response.setDate(date);
        response.setTradeCount(Long.valueOf(volume));
        return response;
    }

    private SegmentTrendResponse createSegmentTrend(String segment, LocalDate date, Integer count) {
        SegmentTrendResponse response = new SegmentTrendResponse();
        response.setSegment(segment);
        response.setDate(date);
        response.setActiveClientCount(Long.valueOf(count));
        return response;
    }

    private FulfillmentTrendResponse createFulfillmentTrend(LocalDate date, Double fulfillmentRate) {
        FulfillmentTrendResponse response = new FulfillmentTrendResponse();
        response.setDate(date);
        response.setFulfillmentRate(new java.math.BigDecimal(fulfillmentRate.toString()));
        return response;
    }

    private AssetClassTrendResponse createAssetClassTrend(String assetClass, LocalDate date, Integer volume) {
        AssetClassTrendResponse response = new AssetClassTrendResponse();
        response.setAssetClass(assetClass);
        response.setDate(date);
        response.setTotalVolume(Long.valueOf(volume));
        return response;
    }
}
