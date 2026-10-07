package com.agentsbackend.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.agentsbackend.services.AnalyticsService;
import com.agentsbackend.DTO.response.PlatformMetricsResponse;
import com.agentsbackend.DTO.response.TopInstrumentResponse;
import com.agentsbackend.DTO.response.AssetClassMetricsResponse;
import com.agentsbackend.DTO.response.OrderMetricsResponse;
import com.agentsbackend.DTO.response.ClientSegmentationResponse;
import com.agentsbackend.DTO.response.TotalVolumeResponse;
import com.agentsbackend.DTO.response.TotalTradesResponse;
import com.agentsbackend.DTO.response.ActiveClientsResponse;
import com.agentsbackend.DTO.response.VolumeTrendResponse;
import com.agentsbackend.DTO.response.InstrumentTrendResponse;
import com.agentsbackend.DTO.response.ClientActivityTrendResponse;
import com.agentsbackend.DTO.response.InstrumentAnalysisResponse;
import com.agentsbackend.DTO.response.SegmentTrendResponse;
import com.agentsbackend.DTO.response.FulfillmentTrendResponse;
import com.agentsbackend.DTO.response.AssetClassTrendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalyticsController - REST API endpoints for platform-wide analytics.
 * All endpoints provide big-picture metrics about the platform.
 * Includes: top stocks, order fulfillment rates, client segments, asset classes.
 * Base path: /api/v1/analytics
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ============================================================
    // PLATFORM METRICS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/platform/metrics
     * Get comprehensive platform metrics and KPIs.
     * Includes: total clients, accounts, AUM, order fulfillment rate, trading volume.
     */
    @GetMapping("/platform/metrics")
    public ResponseEntity<PlatformMetricsResponse> getPlatformMetrics() {
        logger.info("GET /platform/metrics");
        
        PlatformMetricsResponse metrics = analyticsService.getPlatformMetrics();
        return ResponseEntity.ok(metrics);
    }

    // ============================================================
    // TOP INSTRUMENTS & MARKET LEADERS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/instruments/top-traded
     * Get top traded instruments across the entire platform (by trade count).
     * Shows most popular stocks/assets being traded.
     * Note: For dashboard, use /dashboard/most-active-instruments instead.
     */
    @GetMapping("/instruments/top-traded")
    public ResponseEntity<List<TopInstrumentResponse>> getTopTradedInstruments(
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        logger.info("GET /instruments/top-traded?limit={}", limit);
        
        List<TopInstrumentResponse> topInstruments = analyticsService.getTopTradedInstruments(limit);
        return ResponseEntity.ok(topInstruments);
    }

    // ============================================================
    // ASSET CLASS METRICS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/assets/by-class
     * Get asset class distribution metrics across the entire platform.
     * Shows trading activity by asset class (stocks, bonds, crypto, etc.)
     */
    @GetMapping("/assets/by-class")
    public ResponseEntity<List<AssetClassMetricsResponse>> getAssetClassMetrics() {
        logger.info("GET /assets/by-class");
        
        List<AssetClassMetricsResponse> assetClassMetrics = analyticsService.getAssetClassMetrics();
        return ResponseEntity.ok(assetClassMetrics);
    }

    // ============================================================
    // CLIENT SEGMENTATION ENDPOINTS (PLATFORM-WIDE)
    // ============================================================

    /**
     * GET /api/v1/analytics/clients/by-risk-tolerance
     * Get client distribution segmented by risk tolerance level.
     * Shows how many clients are conservative, moderate, or aggressive investors.
     */
    @GetMapping("/clients/by-risk-tolerance")
    public ResponseEntity<List<ClientSegmentationResponse>> getClientSegmentationByRiskTolerance() {
        logger.info("GET /clients/by-risk-tolerance");
        
        List<ClientSegmentationResponse> segmentation = 
            analyticsService.getClientSegmentationByRiskTolerance();
        return ResponseEntity.ok(segmentation);
    }

    /**
     * GET /api/v1/analytics/clients/by-portfolio-size
     * Get client distribution segmented by portfolio size range.
     * Shows how many clients fall into different AUM brackets.
     */
    @GetMapping("/clients/by-portfolio-size")
    public ResponseEntity<List<ClientSegmentationResponse>> getClientSegmentationByPortfolioSize() {
        logger.info("GET /clients/by-portfolio-size");
        
        List<ClientSegmentationResponse> segmentation = 
            analyticsService.getClientSegmentationByPortfolioSize();
        return ResponseEntity.ok(segmentation);
    }

    /**
     * GET /api/v1/analytics/clients/most-active
     * Get the most active clients in the last 30 days (by order count).
     * Shows top traders on the platform.
     */
    @GetMapping("/clients/most-active")
    public ResponseEntity<List<ClientSegmentationResponse>> getMostActiveClients(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        logger.info("GET /clients/most-active?limit={}", limit);
        
        List<ClientSegmentationResponse> activeClients = analyticsService.getMostActiveClients(limit);
        return ResponseEntity.ok(activeClients);
    }

    // ============================================================
    // DASHBOARD TOP METRICS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/total-volume
     * Get total trading volume metrics for the analytics dashboard top section.
     * Shows: total quantity traded, total value, order count, and averages.
     * Optional: Query parameter 'date' for specific date (defaults to today).
     */
    @GetMapping("/dashboard/total-volume")
    public ResponseEntity<TotalVolumeResponse> getTotalVolume(
            @RequestParam(value = "date", required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        logger.info("GET /dashboard/total-volume?date={}", date);
        
        TotalVolumeResponse totalVolume = analyticsService.getDailyTotalVolume(date);
        return ResponseEntity.ok(totalVolume);
    }

    /**
     * GET /api/v1/analytics/dashboard/total-trades
     * Get total trades metrics for the analytics dashboard top section.
     * Shows: total trades, fulfillment rate, cancellation rate, buy/sell split.
     * Optional: Query parameter 'date' for specific date (defaults to today).
     */
    @GetMapping("/dashboard/total-trades")
    public ResponseEntity<TotalTradesResponse> getTotalTrades(
            @RequestParam(value = "date", required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        logger.info("GET /dashboard/total-trades?date={}", date);
        
        TotalTradesResponse totalTrades = analyticsService.getDailyTotalTrades(date);
        return ResponseEntity.ok(totalTrades);
    }

    /**
     * GET /api/v1/analytics/dashboard/active-clients
     * Get active clients metrics for the analytics dashboard top section.
     * Shows: active clients (last 30 days), total clients, active percentage, and accounts.
     * Optional: Query parameter 'date' for specific date (defaults to today).
     */
    @GetMapping("/dashboard/active-clients")
    public ResponseEntity<ActiveClientsResponse> getActiveClients(
            @RequestParam(value = "date", required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        logger.info("GET /dashboard/active-clients?date={}", date);
        
        ActiveClientsResponse activeClients = analyticsService.getDailyActiveClients(date);
        return ResponseEntity.ok(activeClients);
    }

    // ============================================================
    // DASHBOARD TREND ANALYSIS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/volume-trend
     * Get trading volume trend over a date range for the volume trends chart.
     * Provides time series data for line/bar charts showing trading activity over time.
     * Query parameters:
     *   - startDate: Start date (YYYY-MM-DD) - required
     *   - endDate: End date (YYYY-MM-DD) - required
     * Returns daily aggregated volume data.
     */
    @GetMapping("/dashboard/volume-trend")
    public ResponseEntity<List<VolumeTrendResponse>> getVolumeTrend(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("GET /dashboard/volume-trend?startDate={}&endDate={}", startDate, endDate);
        
        try {
            List<VolumeTrendResponse> volumeTrend = analyticsService.getVolumeTrend(startDate, endDate);
            return ResponseEntity.ok(volumeTrend);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid date range for volume trend", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/v1/analytics/dashboard/instrument-trend
     * Get top instruments trading activity trend over a date range.
     * Used for displaying which instruments have been most active over time.
     * Query parameters:
     *   - startDate: Start date (YYYY-MM-DD) - required
     *   - endDate: End date (YYYY-MM-DD) - required
     * Returns daily trading data for each instrument.
     */
    @GetMapping("/dashboard/instrument-trend")
    public ResponseEntity<List<InstrumentTrendResponse>> getInstrumentTrend(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("GET /dashboard/instrument-trend?startDate={}&endDate={}", startDate, endDate);
        
        try {
            List<InstrumentTrendResponse> instrumentTrend = analyticsService.getInstrumentTrendByDateRange(startDate, endDate);
            return ResponseEntity.ok(instrumentTrend);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid date range for instrument trend", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/v1/analytics/dashboard/most-active-instruments
     * Get the most active instruments for a specific date.
     * Used for the "Most Active Instruments" section on dashboard.
     * Query parameters:
     *   - date: The date to analyze (YYYY-MM-DD) - optional (defaults to today)
     *   - limit: Number of top instruments to return - optional (defaults to 20)
     */
    @GetMapping("/dashboard/most-active-instruments")
    public ResponseEntity<List<InstrumentTrendResponse>> getMostActiveInstruments(
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        if (date == null) {
            date = LocalDate.now();
        }
        logger.info("GET /dashboard/most-active-instruments?date={}&limit={}", date, limit);
        
        List<InstrumentTrendResponse> activeInstruments = analyticsService.getTopInstrumentsForDate(date, limit);
        return ResponseEntity.ok(activeInstruments);
    }

    // ============================================================
    // PRIORITY 1: CLIENT ACTIVITY TRENDS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/client-activity-trend
     * Get client activity trends over a date range.
     * Query params: startDate, endDate, limit
     */
    @GetMapping("/dashboard/client-activity-trend")
    public ResponseEntity<List<ClientActivityTrendResponse>> clientActivityTrend(
            @RequestParam(value = "startDate") LocalDate startDate,
            @RequestParam(value = "endDate") LocalDate endDate,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        logger.info("GET /dashboard/client-activity-trend?startDate={}&endDate={}&limit={}", startDate, endDate, limit);
        
        List<ClientActivityTrendResponse> trend = analyticsService.getClientActivityTrend(startDate, endDate, limit);
        return ResponseEntity.ok(trend);
    }

    // ============================================================
    // PRIORITY 2: INSTRUMENT ANALYSIS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/instrument-analysis
     * Get detailed analysis for a specific instrument over a date range.
     * Query params: ticker, startDate, endDate
     */
    @GetMapping("/dashboard/instrument-analysis")
    public ResponseEntity<InstrumentAnalysisResponse> instrumentAnalysis(
            @RequestParam(value = "ticker") String ticker,
            @RequestParam(value = "startDate") LocalDate startDate,
            @RequestParam(value = "endDate") LocalDate endDate) {
        logger.info("GET /dashboard/instrument-analysis?ticker={}&startDate={}&endDate={}", ticker, startDate, endDate);
        
        InstrumentAnalysisResponse analysis = analyticsService.getInstrumentAnalysis(ticker, startDate, endDate);
        return ResponseEntity.ok(analysis);
    }

    // ============================================================
    // PRIORITY 3: CLIENT SEGMENT TREND ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/segment-trends
     * Get client segment trends by risk tolerance or portfolio size.
     * Query params: segmentType, startDate, endDate
     */
    @GetMapping("/dashboard/segment-trends")
    public ResponseEntity<List<SegmentTrendResponse>> segmentTrends(
            @RequestParam(value = "segmentType") String segmentType,
            @RequestParam(value = "startDate") LocalDate startDate,
            @RequestParam(value = "endDate") LocalDate endDate) {
        logger.info("GET /dashboard/segment-trends?segmentType={}&startDate={}&endDate={}", segmentType, startDate, endDate);
        
        List<SegmentTrendResponse> trends = analyticsService.getSegmentTrend(segmentType, startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    // ============================================================
    // PRIORITY 4: FULFILLMENT TREND ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/fulfillment-trends
     * Get order fulfillment trends over a date range.
     * Query params: startDate, endDate
     */
    @GetMapping("/dashboard/fulfillment-trends")
    public ResponseEntity<List<FulfillmentTrendResponse>> fulfillmentTrends(
            @RequestParam(value = "startDate") LocalDate startDate,
            @RequestParam(value = "endDate") LocalDate endDate) {
        logger.info("GET /dashboard/fulfillment-trends?startDate={}&endDate={}", startDate, endDate);
        
        List<FulfillmentTrendResponse> trends = analyticsService.getFulfillmentTrend(startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    // ============================================================
    // PRIORITY 5: ASSET CLASS TREND ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/dashboard/asset-class-trends
     * Get asset class trading trends over a date range.
     * Query params: startDate, endDate
     */
    @GetMapping("/dashboard/asset-class-trends")
    public ResponseEntity<List<AssetClassTrendResponse>> assetClassTrends(
            @RequestParam(value = "startDate") LocalDate startDate,
            @RequestParam(value = "endDate") LocalDate endDate) {
        logger.info("GET /dashboard/asset-class-trends?startDate={}&endDate={}", startDate, endDate);
        
        List<AssetClassTrendResponse> trends = analyticsService.getAssetClassTrend(startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    // ============================================================
    // HEALTH CHECK ENDPOINT
    // ============================================================

    /**
     * GET /api/v1/analytics/health
     * Simple health check for analytics service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        logger.info("GET /health");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AnalyticsService");
        response.put("description", "Platform-wide analytics engine");
        response.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
}
