package com.agentsbackend.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.time.LocalDate;

import com.agentsbackend.repos.analytics.AnalyticsRepository;
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

/**
 * AnalyticsServiceImpl - Implementation of AnalyticsService.
 * Provides platform-wide analytics operations based on warehouse schema data.
 * Focus: Big-picture metrics, not individual account/client analytics.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);
    private final AnalyticsRepository analyticsRepository;

    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    // ============================================================
    // PLATFORM-WIDE METRICS IMPLEMENTATION
    // ============================================================

    @Override
    public PlatformMetricsResponse getPlatformMetrics() {
        logger.info("Fetching platform-wide metrics and KPIs");
        return analyticsRepository.getPlatformMetrics();
    }

    // ============================================================
    // TOP INSTRUMENTS & MARKET LEADERS IMPLEMENTATION
    // ============================================================

    @Override
    public List<TopInstrumentResponse> getTopTradedInstruments(Integer limit) {
        logger.info("Fetching top {} traded instruments by trade count", limit);
        
        if (limit == null || limit <= 0) {
            limit = 20; // Default limit
        }
        
        return analyticsRepository.getTopTradedInstruments(limit);
    }

    @Override
    public List<TopInstrumentResponse> getTopInstrumentsByVolume(Integer limit) {
        logger.info("Fetching top {} instruments by volume value", limit);
        
        if (limit == null || limit <= 0) {
            limit = 20; // Default limit
        }
        
        return analyticsRepository.getTopInstrumentsByVolume(limit);
    }

    // ============================================================
    // ASSET CLASS ANALYTICS IMPLEMENTATION
    // ============================================================

    @Override
    public List<AssetClassMetricsResponse> getAssetClassMetrics() {
        logger.info("Fetching asset class metrics across platform");
        return analyticsRepository.getAssetClassMetrics();
    }

    // ============================================================
    // ORDER FULFILLMENT & EXECUTION METRICS IMPLEMENTATION
    // ============================================================

    @Override
    public OrderMetricsResponse getOrderMetrics() {
        logger.info("Fetching platform-wide order fulfillment metrics");
        return analyticsRepository.getOrderMetrics();
    }

    @Override
    public OrderMetricsResponse getOrderMetricsByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching order metrics from {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getOrderMetricsByDateRange(startDate, endDate);
    }

    // ============================================================
    // CLIENT SEGMENTATION IMPLEMENTATION (PLATFORM-WIDE)
    // ============================================================

    @Override
    public List<ClientSegmentationResponse> getClientSegmentationByRiskTolerance() {
        logger.info("Fetching client segmentation by risk tolerance");
        return analyticsRepository.getClientSegmentationByRiskTolerance();
    }

    @Override
    public List<ClientSegmentationResponse> getClientSegmentationByPortfolioSize() {
        logger.info("Fetching client segmentation by portfolio size");
        return analyticsRepository.getClientSegmentationByPortfolioSize();
    }

    @Override
    public List<ClientSegmentationResponse> getMostActiveClients(Integer limit) {
        logger.info("Fetching top {} most active clients in last 30 days", limit);
        
        if (limit == null || limit <= 0) {
            limit = 10; // Default limit
        }
        
        return analyticsRepository.getMostActiveClients(limit);
    }

    // ============================================================
    // DASHBOARD TOP METRICS IMPLEMENTATION
    // ============================================================

    @Override
    public TotalVolumeResponse getTotalVolume() {
        logger.info("Fetching total volume metrics for dashboard");
        return analyticsRepository.getTotalVolume();
    }

    @Override
    public TotalTradesResponse getTotalTrades() {
        logger.info("Fetching total trades metrics for dashboard");
        return analyticsRepository.getTotalTrades();
    }

    @Override
    public ActiveClientsResponse getActiveClients() {
        logger.info("Fetching active clients metrics for dashboard");
        return analyticsRepository.getActiveClients();
    }

    // ============================================================
    // DAILY METRICS IMPLEMENTATION
    // ============================================================

    @Override
    public TotalVolumeResponse getDailyTotalVolume(LocalDate date) {
        logger.info("Fetching daily total volume metrics for {}", date);
        return analyticsRepository.getDailyTotalVolume(date);
    }

    @Override
    public TotalTradesResponse getDailyTotalTrades(LocalDate date) {
        logger.info("Fetching daily total trades metrics for {}", date);
        return analyticsRepository.getDailyTotalTrades(date);
    }

    @Override
    public ActiveClientsResponse getDailyActiveClients(LocalDate date) {
        logger.info("Fetching daily active clients metrics for {}", date);
        return analyticsRepository.getDailyActiveClients(date);
    }

    // ============================================================
    // TREND ANALYSIS IMPLEMENTATION
    // ============================================================

    @Override
    public List<VolumeTrendResponse> getVolumeTrend(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching volume trend from {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getVolumeTrend(startDate, endDate);
    }

    @Override
    public List<InstrumentTrendResponse> getInstrumentTrendByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching instrument trend from {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getInstrumentTrendByDateRange(startDate, endDate);
    }

    @Override
    public List<InstrumentTrendResponse> getTopInstrumentsForDate(LocalDate date, Integer limit) {
        logger.info("Fetching top {} instruments for {}", limit, date);
        
        if (limit == null || limit <= 0) {
            limit = 20; // Default limit
        }
        
        return analyticsRepository.getTopInstrumentsForDate(date, limit);
    }

    // ============================================================
    // PRIORITY 1: CLIENT ACTIVITY TRENDS IMPLEMENTATION
    // ============================================================

    @Override
    public List<ClientActivityTrendResponse> getClientActivityTrend(LocalDate startDate, LocalDate endDate, Integer limit) {
        logger.info("Fetching client activity trends from {} to {} for top {} clients", startDate, endDate, limit);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        if (limit == null || limit <= 0) {
            limit = 10; // Default limit
        }
        
        return analyticsRepository.getClientActivityTrend(startDate, endDate, limit);
    }

    // ============================================================
    // PRIORITY 2: INSTRUMENT ANALYSIS IMPLEMENTATION
    // ============================================================

    @Override
    public InstrumentAnalysisResponse getInstrumentAnalysis(String ticker, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching instrument analysis for {} from {} to {}", ticker, startDate, endDate);
        
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker symbol is required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getInstrumentAnalysis(ticker, startDate, endDate);
    }

    // ============================================================
    // PRIORITY 3: CLIENT SEGMENT TRENDS IMPLEMENTATION
    // ============================================================

    @Override
    public List<SegmentTrendResponse> getSegmentTrend(String segmentType, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching segment trends by {} from {} to {}", segmentType, startDate, endDate);
        
        if (segmentType == null || segmentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Segment type is required (RISK_TOLERANCE or PORTFOLIO_SIZE)");
        }
        
        if (!segmentType.equals("RISK_TOLERANCE") && !segmentType.equals("PORTFOLIO_SIZE")) {
            throw new IllegalArgumentException("Invalid segment type. Must be RISK_TOLERANCE or PORTFOLIO_SIZE");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getSegmentTrend(segmentType, startDate, endDate);
    }

    // ============================================================
    // PRIORITY 4: FULFILLMENT TRENDS IMPLEMENTATION
    // ============================================================

    @Override
    public List<FulfillmentTrendResponse> getFulfillmentTrend(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching fulfillment trends from {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getFulfillmentTrend(startDate, endDate);
    }

    // ============================================================
    // PRIORITY 5: ASSET CLASS TRENDS IMPLEMENTATION
    // ============================================================

    @Override
    public List<AssetClassTrendResponse> getAssetClassTrend(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching asset class trends from {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        return analyticsRepository.getAssetClassTrend(startDate, endDate);
    }
}
