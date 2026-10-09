package com.agentsbackend.controllers;

import com.agentsbackend.services.AnalyticsService;
import com.agentsbackend.DTO.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AnalyticsController analyticsController = new AnalyticsController(analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /platform/metrics - should return platform metrics")
    void testGetPlatformMetrics() throws Exception {
        // Arrange
        PlatformMetricsResponse response = new PlatformMetricsResponse();
        response.setTotalClients(1000L);
        response.setTotalAccounts(1500L);
        response.setTotalAssetsUnderManagement(new java.math.BigDecimal("5000000"));
        response.setOrderFulfillmentRate(new java.math.BigDecimal("95.5"));

        when(analyticsService.getPlatformMetrics()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/platform/metrics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalClients").value(1000L))
            .andExpect(jsonPath("$.totalAccounts").value(1500L))
            .andExpect(jsonPath("$.totalAssetsUnderManagement").value(5000000));

        verify(analyticsService).getPlatformMetrics();
    }

    @Test
    @DisplayName("GET /instruments/top-traded - should return top traded instruments")
    void testGetTopTradedInstruments() throws Exception {
        // Arrange
        List<TopInstrumentResponse> instruments = Arrays.asList(
            createTopInstrument("AAPL", "Apple Inc", 150),
            createTopInstrument("GOOGL", "Alphabet Inc", 120)
        );

        when(analyticsService.getTopTradedInstruments(20)).thenReturn(instruments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/instruments/top-traded")
                .param("limit", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticker").value("AAPL"))
            .andExpect(jsonPath("$[1].ticker").value("GOOGL"))
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getTopTradedInstruments(20);
    }

    @Test
    @DisplayName("GET /assets/by-class - should return asset class metrics")
    void testGetAssetClassMetrics() throws Exception {
        // Arrange
        List<AssetClassMetricsResponse> metrics = Arrays.asList(
            createAssetClassMetric("STOCK", 50000),
            createAssetClassMetric("BOND", 30000)
        );

        when(analyticsService.getAssetClassMetrics()).thenReturn(metrics);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/assets/by-class")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].assetClass").value("STOCK"))
            .andExpect(jsonPath("$[1].assetClass").value("BOND"))
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getAssetClassMetrics();
    }

    @Test
    @DisplayName("GET /clients/by-risk-tolerance - should return client segmentation by risk tolerance")
    void testGetClientSegmentationByRiskTolerance() throws Exception {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("CONSERVATIVE", 300),
            createClientSegmentation("AGGRESSIVE", 400)
        );

        when(analyticsService.getClientSegmentationByRiskTolerance()).thenReturn(segmentation);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/clients/by-risk-tolerance")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].segment").value("CONSERVATIVE"))
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getClientSegmentationByRiskTolerance();
    }

    @Test
    @DisplayName("GET /clients/by-portfolio-size - should return client segmentation by portfolio size")
    void testGetClientSegmentationByPortfolioSize() throws Exception {
        // Arrange
        List<ClientSegmentationResponse> segmentation = Arrays.asList(
            createClientSegmentation("SMALL", 200),
            createClientSegmentation("LARGE", 150)
        );

        when(analyticsService.getClientSegmentationByPortfolioSize()).thenReturn(segmentation);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/clients/by-portfolio-size")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getClientSegmentationByPortfolioSize();
    }

    @Test
    @DisplayName("GET /dashboard/total-volume - should return total volume for date")
    void testGetTotalVolume() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalVolumeResponse response = new TotalVolumeResponse();
        response.setTotalQuantityTraded(50000L);
        response.setTotalValueTraded(1500000.0);

        when(analyticsService.getDailyTotalVolume(date)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/total-volume")
                .param("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalQuantityTraded").value(50000))
            .andExpect(jsonPath("$.totalValueTraded").value(1500000.0));

        verify(analyticsService).getDailyTotalVolume(date);
    }

    @Test
    @DisplayName("GET /dashboard/total-trades - should return total trades for date")
    void testGetTotalTrades() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        TotalTradesResponse response = new TotalTradesResponse();
        response.setTotalTrades(500L);
        response.setFulfillmentRate(96.2);

        when(analyticsService.getDailyTotalTrades(date)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/total-trades")
                .param("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTrades").value(500))
            .andExpect(jsonPath("$.fulfillmentRate").value(96.2));

        verify(analyticsService).getDailyTotalTrades(date);
    }

    @Test
    @DisplayName("GET /dashboard/active-clients - should return active clients for date")
    void testGetActiveClients() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        ActiveClientsResponse response = new ActiveClientsResponse();
        response.setTotalActiveClients(350L);
        response.setTotalClients(1000L);
        response.setActiveClientPercentage(35.0);

        when(analyticsService.getDailyActiveClients(date)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/active-clients")
                .param("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalActiveClients").value(350L))
            .andExpect(jsonPath("$.totalClients").value(1000));

        verify(analyticsService).getDailyActiveClients(date);
    }

    @Test
    @DisplayName("GET /dashboard/volume-trend - should return volume trend")
    void testGetVolumeTrend() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<VolumeTrendResponse> trends = Arrays.asList(
            createVolumeTrend(startDate, 40000),
            createVolumeTrend(startDate.plusDays(1), 42000)
        );

        when(analyticsService.getVolumeTrend(startDate, endDate)).thenReturn(trends);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/volume-trend")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getVolumeTrend(startDate, endDate);
    }

    @Test
    @DisplayName("GET /dashboard/instrument-trend - should return instrument trend")
    void testGetInstrumentTrend() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<InstrumentTrendResponse> trends = Arrays.asList(
            createInstrumentTrend("AAPL", startDate, 100),
            createInstrumentTrend("GOOGL", startDate, 80)
        );

        when(analyticsService.getInstrumentTrendByDateRange(startDate, endDate)).thenReturn(trends);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/instrument-trend")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getInstrumentTrendByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("GET /dashboard/most-active-instruments - should return top instruments")
    void testGetMostActiveInstruments() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        List<InstrumentTrendResponse> instruments = Arrays.asList(
            createInstrumentTrend("AAPL", date, 120),
            createInstrumentTrend("MSFT", date, 100)
        );

        when(analyticsService.getTopInstrumentsForDate(date, 20)).thenReturn(instruments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/most-active-instruments")
                .param("date", date.toString())
                .param("limit", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getTopInstrumentsForDate(date, 20);
    }

    @Test
    @DisplayName("GET /dashboard/instrument-analysis - should return instrument analysis")
    void testGetInstrumentAnalysis() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        InstrumentAnalysisResponse response = new InstrumentAnalysisResponse();
        response.setTicker("AAPL");
        response.setTotalVolume(100000L);
        response.setTradeCount(500L);

        when(analyticsService.getInstrumentAnalysis("AAPL", startDate, endDate))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/instrument-analysis")
                .param("ticker", "AAPL")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticker").value("AAPL"))
            .andExpect(jsonPath("$.totalVolume").value(100000));

        verify(analyticsService).getInstrumentAnalysis("AAPL", startDate, endDate);
    }

    @Test
    @DisplayName("GET /dashboard/segment-trends - should return segment trends")
    void testGetSegmentTrends() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<SegmentTrendResponse> trends = Arrays.asList(
            createSegmentTrend("CONSERVATIVE", startDate, 300),
            createSegmentTrend("AGGRESSIVE", startDate, 400)
        );

        when(analyticsService.getSegmentTrend("RISK_TOLERANCE", startDate, endDate))
            .thenReturn(trends);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/segment-trends")
                .param("segmentType", "RISK_TOLERANCE")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getSegmentTrend("RISK_TOLERANCE", startDate, endDate);
    }

    @Test
    @DisplayName("GET /dashboard/fulfillment-trends - should return fulfillment trends")
    void testGetFulfillmentTrends() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<FulfillmentTrendResponse> trends = Arrays.asList(
            createFulfillmentTrend(startDate, 95.5),
            createFulfillmentTrend(startDate.plusDays(1), 96.2)
        );

        when(analyticsService.getFulfillmentTrend(startDate, endDate)).thenReturn(trends);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/fulfillment-trends")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getFulfillmentTrend(startDate, endDate);
    }

    @Test
    @DisplayName("GET /dashboard/asset-class-trends - should return asset class trends")
    void testGetAssetClassTrends() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<AssetClassTrendResponse> trends = Arrays.asList(
            createAssetClassTrend("STOCK", startDate, 40000),
            createAssetClassTrend("BOND", startDate, 20000)
        );

        when(analyticsService.getAssetClassTrend(startDate, endDate)).thenReturn(trends);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard/asset-class-trends")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(analyticsService).getAssetClassTrend(startDate, endDate);
    }

    @Test
    @DisplayName("GET /health - should return health check status")
    void testHealthCheck() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/health")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("AnalyticsService"));
    }

    // Helper methods to create test data
    private TopInstrumentResponse createTopInstrument(String ticker, String name, Integer tradeCount) {
        TopInstrumentResponse response = new TopInstrumentResponse();
        response.setTicker(ticker);
        response.setInstrumentName(name);
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
