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
import com.agentsbackend.DTO.response.ClientSegmentationResponse;
import com.agentsbackend.DTO.response.TotalVolumeResponse;
import com.agentsbackend.DTO.response.TotalTradesResponse;
import com.agentsbackend.DTO.response.ActiveClientsResponse;
import com.agentsbackend.DTO.response.VolumeTrendResponse;
import com.agentsbackend.DTO.response.InstrumentTrendResponse;
import com.agentsbackend.DTO.response.InstrumentAnalysisResponse;
import com.agentsbackend.DTO.response.SegmentTrendResponse;
import com.agentsbackend.DTO.response.FulfillmentTrendResponse;
import com.agentsbackend.DTO.response.AssetClassTrendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AnalyticsController - REST API endpoints for platform-wide analytics.
 * All endpoints provide big-picture metrics about the platform.
 * Includes: top stocks, order fulfillment rates, client segments, asset classes.
 * Base path: /api/v1/analytics
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Platform-wide metrics and dashboard trend analytics")
@SecurityRequirement(name = "bearerAuth")
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
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
    @Operation(summary = "Get platform metrics", description = "Returns the comprehensive platform KPI snapshot.")
    @ApiResponse(responseCode = "200", description = "Platform metrics returned")
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
        @Operation(summary = "Get top traded instruments", description = "Returns instruments ranked by platform-wide trade count.")
        @ApiResponse(responseCode = "200", description = "Top traded instruments returned")
    public ResponseEntity<List<TopInstrumentResponse>> getTopTradedInstruments(
            @Parameter(description = "Maximum number of instruments to return", example = "20")
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
    @Operation(summary = "Get asset class metrics", description = "Returns platform-wide trading activity grouped by asset class.")
    @ApiResponse(responseCode = "200", description = "Asset class metrics returned")
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
    @Operation(summary = "Get client metrics by risk tolerance", description = "Returns client distribution grouped by risk tolerance.")
    @ApiResponse(responseCode = "200", description = "Client segmentation returned")
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
    @Operation(summary = "Get client metrics by portfolio size", description = "Returns client distribution grouped by portfolio size.")
    @ApiResponse(responseCode = "200", description = "Client segmentation returned")
    public ResponseEntity<List<ClientSegmentationResponse>> getClientSegmentationByPortfolioSize() {
        logger.info("GET /clients/by-portfolio-size");
        
        List<ClientSegmentationResponse> segmentation = 
            analyticsService.getClientSegmentationByPortfolioSize();
        return ResponseEntity.ok(segmentation);
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
        @Operation(summary = "Get daily total volume", description = "Returns total quantity, value, order count, and averages for a date.")
        @ApiResponse(responseCode = "200", description = "Daily volume metrics returned")
    public ResponseEntity<TotalVolumeResponse> getTotalVolume(
            @Parameter(description = "Date to analyze; defaults to today", example = "2026-10-19")
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
        @Operation(summary = "Get daily total trades", description = "Returns trade counts, fulfillment rate, cancellation rate, and buy/sell split for a date.")
        @ApiResponse(responseCode = "200", description = "Daily trade metrics returned")
    public ResponseEntity<TotalTradesResponse> getTotalTrades(
            @Parameter(description = "Date to analyze; defaults to today", example = "2026-10-19")
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
        @Operation(summary = "Get daily active clients", description = "Returns active client, account, and activity percentage metrics for a date.")
        @ApiResponse(responseCode = "200", description = "Active client metrics returned")
    public ResponseEntity<ActiveClientsResponse> getActiveClients(
            @Parameter(description = "Date to analyze; defaults to today", example = "2026-10-19")
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
    @Operation(summary = "Get volume trend", description = "Returns daily aggregated trading volume for a date range.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Volume trend returned"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<VolumeTrendResponse>> getVolumeTrend(
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
    @Operation(summary = "Get instrument trend", description = "Returns daily trading activity for instruments over a date range.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Instrument trend returned"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<InstrumentTrendResponse>> getInstrumentTrend(
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
        @Operation(summary = "Get most active instruments", description = "Returns the most active instruments for a date.")
        @ApiResponse(responseCode = "200", description = "Most active instruments returned")
    public ResponseEntity<List<InstrumentTrendResponse>> getMostActiveInstruments(
            @Parameter(description = "Date to analyze; defaults to today", example = "2026-10-19")
            @RequestParam(value = "date", required = false) LocalDate date,
            @Parameter(description = "Maximum number of instruments to return", example = "20")
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        if (date == null) {
            date = LocalDate.now();
        }
        logger.info("GET /dashboard/most-active-instruments?date={}&limit={}", date, limit);
        
        List<InstrumentTrendResponse> activeInstruments = analyticsService.getTopInstrumentsForDate(date, limit);
        return ResponseEntity.ok(activeInstruments);
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
        @Operation(summary = "Analyze an instrument", description = "Returns detailed metrics for one instrument over a date range.")
        @ApiResponse(responseCode = "200", description = "Instrument analysis returned")
    public ResponseEntity<InstrumentAnalysisResponse> instrumentAnalysis(
            @Parameter(description = "Instrument ticker", example = "AAPL")
            @RequestParam(value = "ticker") String ticker,
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam(value = "startDate") LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
        @Operation(summary = "Get client segment trends", description = "Returns client trading trends grouped by risk tolerance or portfolio size.")
        @ApiResponse(responseCode = "200", description = "Segment trends returned")
    public ResponseEntity<List<SegmentTrendResponse>> segmentTrends(
            @Parameter(description = "Segment dimension", example = "RISK_TOLERANCE")
            @RequestParam(value = "segmentType") String segmentType,
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam(value = "startDate") LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
        @Operation(summary = "Get fulfillment trends", description = "Returns daily order fulfillment metrics for a date range.")
        @ApiResponse(responseCode = "200", description = "Fulfillment trends returned")
    public ResponseEntity<List<FulfillmentTrendResponse>> fulfillmentTrends(
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam(value = "startDate") LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
        @Operation(summary = "Get asset class trends", description = "Returns daily trading trends grouped by asset class for a date range.")
        @ApiResponse(responseCode = "200", description = "Asset class trends returned")
    public ResponseEntity<List<AssetClassTrendResponse>> assetClassTrends(
            @Parameter(description = "Inclusive start date", example = "2026-10-01")
            @RequestParam(value = "startDate") LocalDate startDate,
            @Parameter(description = "Inclusive end date", example = "2026-10-19")
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
    @Operation(summary = "Check analytics health", description = "Returns the current analytics service health status.")
    @ApiResponse(responseCode = "200", description = "Analytics service is available")
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
