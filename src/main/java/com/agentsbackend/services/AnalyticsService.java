package com.agentsbackend.services;

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

    // ============================================================
    // DASHBOARD TOP METRICS
    // ============================================================

    /**
     * Get total volume metrics for analytics dashboard.
     * @return Total volume, value, and order count metrics
     */
    TotalVolumeResponse getTotalVolume();

    /**
     * Get total trades metrics for analytics dashboard.
     * @return Total trades, fulfillment rate, and buy/sell split
     */
    TotalTradesResponse getTotalTrades();

    /**
     * Get active clients metrics for analytics dashboard.
     * @return Count of active clients and accounts
     */
    ActiveClientsResponse getActiveClients();

    // ============================================================
    // DAILY METRICS (For specific date)
    // ============================================================

    /**
     * Get total volume metrics for a specific date.
     * @param date The date to get metrics for
     * @return Total volume for the day
     */
    TotalVolumeResponse getDailyTotalVolume(LocalDate date);

    /**
     * Get total trades metrics for a specific date.
     * @param date The date to get metrics for
     * @return Trade metrics for the day
     */
    TotalTradesResponse getDailyTotalTrades(LocalDate date);

    /**
     * Get active clients metrics for a specific date.
     * @param date The date to get metrics for
     * @return Active clients count for the day
     */
    ActiveClientsResponse getDailyActiveClients(LocalDate date);

    // ============================================================
    // TREND ANALYSIS (Time series data)
    // ============================================================

    /**
     * Get trading volume trend over a date range.
     * Used for volume trend chart on dashboard.
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily volume data points
     */
    List<VolumeTrendResponse> getVolumeTrend(LocalDate startDate, LocalDate endDate);

    /**
     * Get top instruments trading activity trend over a date range.
     * Used for instrument trends analysis.
     * @param startDate Start date
     * @param endDate End date
     * @return List of instrument trading trends
     */
    List<InstrumentTrendResponse> getInstrumentTrendByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get top N active instruments for a specific date.
     * @param date The date to analyze
     * @param limit Number of top instruments
     * @return List of top instruments for that day
     */
    List<InstrumentTrendResponse> getTopInstrumentsForDate(LocalDate date, Integer limit);

    // ============================================================
    // PRIORITY 2: INSTRUMENT ANALYSIS
    // ============================================================

    /**
     * Get detailed analysis for a specific instrument over a date range.
     * @param ticker Instrument ticker symbol
     * @param startDate Start date
     * @param endDate End date
     * @return Instrument analysis with comprehensive metrics
     */
    InstrumentAnalysisResponse getInstrumentAnalysis(String ticker, LocalDate startDate, LocalDate endDate);

    // ============================================================
    // PRIORITY 3: CLIENT SEGMENT TRENDS
    // ============================================================

    /**
     * Get client segment trends by risk tolerance or portfolio size.
     * @param segmentType RISK_TOLERANCE or PORTFOLIO_SIZE
     * @param startDate Start date
     * @param endDate End date
     * @return List of segment trends over time
     */
    List<SegmentTrendResponse> getSegmentTrend(String segmentType, LocalDate startDate, LocalDate endDate);

    // ============================================================
    // PRIORITY 4: FULFILLMENT TRENDS
    // ============================================================

    /**
     * Get order fulfillment trends over a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily fulfillment metrics
     */
    List<FulfillmentTrendResponse> getFulfillmentTrend(LocalDate startDate, LocalDate endDate);

    // ============================================================
    // PRIORITY 5: ASSET CLASS TRENDS
    // ============================================================

    /**
     * Get asset class trading trends over a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of asset class trends over time
     */
    List<AssetClassTrendResponse> getAssetClassTrend(LocalDate startDate, LocalDate endDate);
}
