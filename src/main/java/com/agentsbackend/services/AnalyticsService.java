package com.agentsbackend.services;

import com.agentsbackend.DTO.response.PlatformMetricsResponse;
import com.agentsbackend.DTO.response.TopInstrumentResponse;
import com.agentsbackend.DTO.response.AssetClassMetricsResponse;
import com.agentsbackend.DTO.response.OrderMetricsResponse;
import com.agentsbackend.DTO.response.ClientSegmentationResponse;

import java.util.List;
import java.time.LocalDate;

/**
 * AnalyticsService - Service interface for platform-wide analytics operations.
 * All analytics focus on big-picture platform metrics, not individual accounts.
 * Provides platform KPIs, top instruments, market segments, and fulfillment metrics.
 */
public interface AnalyticsService {

    // ============================================================
    // PLATFORM-WIDE METRICS
    // ============================================================

    /**
     * Get comprehensive platform metrics and KPIs.
     * @return Platform-wide aggregated metrics
     */
    PlatformMetricsResponse getPlatformMetrics();

    // ============================================================
    // TOP INSTRUMENTS & MARKET LEADERS
    // ============================================================

    /**
     * Get top traded instruments by trade count.
     * @param limit Maximum number of instruments
     * @return List of top traded instruments
     */
    List<TopInstrumentResponse> getTopTradedInstruments(Integer limit);

    /**
     * Get top instruments by trading volume (in dollars).
     * @param limit Maximum number of instruments
     * @return List of top instruments by volume value
     */
    List<TopInstrumentResponse> getTopInstrumentsByVolume(Integer limit);

    // ============================================================
    // ASSET CLASS ANALYTICS
    // ============================================================

    /**
     * Get metrics aggregated by asset class across platform.
     * @return List of asset class metrics
     */
    List<AssetClassMetricsResponse> getAssetClassMetrics();

    // ============================================================
    // ORDER FULFILLMENT & EXECUTION METRICS
    // ============================================================

    /**
     * Get platform-wide order fulfillment and execution metrics.
     * @return Order metrics including fill rates and cancellation rates
     */
    OrderMetricsResponse getOrderMetrics();

    /**
     * Get order metrics for a specific date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Order metrics for the period
     */
    OrderMetricsResponse getOrderMetricsByDateRange(LocalDate startDate, LocalDate endDate);

    // ============================================================
    // CLIENT SEGMENTATION (PLATFORM-WIDE)
    // ============================================================

    /**
     * Get client segmentation by risk tolerance across platform.
     * @return List of client segments
     */
    List<ClientSegmentationResponse> getClientSegmentationByRiskTolerance();

    /**
     * Get client segmentation by portfolio size range across platform.
     * @return List of client segments
     */
    List<ClientSegmentationResponse> getClientSegmentationByPortfolioSize();

    /**
     * Get most active clients in the last 30 days by order count.
     * @param limit Maximum number of clients
     * @return List of most active clients
     */
    List<ClientSegmentationResponse> getMostActiveClients(Integer limit);
}
