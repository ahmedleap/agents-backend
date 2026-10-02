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
     */
    @GetMapping("/instruments/top-traded")
    public ResponseEntity<List<TopInstrumentResponse>> getTopTradedInstruments(
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        logger.info("GET /instruments/top-traded?limit={}", limit);
        
        List<TopInstrumentResponse> topInstruments = analyticsService.getTopTradedInstruments(limit);
        return ResponseEntity.ok(topInstruments);
    }

    /**
     * GET /api/v1/analytics/instruments/top-volume
     * Get top instruments by trading volume (total dollar value).
     * Shows instruments with highest monetary trading activity.
     */
    @GetMapping("/instruments/top-volume")
    public ResponseEntity<List<TopInstrumentResponse>> getTopInstrumentsByVolume(
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        logger.info("GET /instruments/top-volume?limit={}", limit);
        
        List<TopInstrumentResponse> topInstruments = analyticsService.getTopInstrumentsByVolume(limit);
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
    // ORDER FULFILLMENT & EXECUTION METRICS ENDPOINTS
    // ============================================================

    /**
     * GET /api/v1/analytics/orders/metrics
     * Get platform-wide order fulfillment and execution metrics.
     * Includes: fulfillment rate, cancellation rate, average order size, buy/sell split.
     */
    @GetMapping("/orders/metrics")
    public ResponseEntity<OrderMetricsResponse> getOrderMetrics() {
        logger.info("GET /orders/metrics");
        
        OrderMetricsResponse orderMetrics = analyticsService.getOrderMetrics();
        return ResponseEntity.ok(orderMetrics);
    }

    /**
     * GET /api/v1/analytics/orders/metrics-by-date
     * Get order metrics for a specific date range.
     * Allows analysis of order trends over time periods.
     */
    @GetMapping("/orders/metrics-by-date")
    public ResponseEntity<OrderMetricsResponse> getOrderMetricsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("GET /orders/metrics-by-date?startDate={}&endDate={}", startDate, endDate);
        
        try {
            OrderMetricsResponse orderMetrics = analyticsService.getOrderMetricsByDateRange(startDate, endDate);
            return ResponseEntity.ok(orderMetrics);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid date range for order metrics", e);
            return ResponseEntity.badRequest().build();
        }
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
