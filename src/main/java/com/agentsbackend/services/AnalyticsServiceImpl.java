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
}
