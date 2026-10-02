package com.agentsbackend.repos.analytics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.DimClient;
import com.agentsbackend.entities.DimAccount;
import com.agentsbackend.DTO.response.PlatformMetricsResponse;
import com.agentsbackend.DTO.response.TopInstrumentResponse;
import com.agentsbackend.DTO.response.AssetClassMetricsResponse;
import com.agentsbackend.DTO.response.OrderMetricsResponse;
import com.agentsbackend.DTO.response.ClientSegmentationResponse;

import java.util.List;
import java.time.LocalDate;

/**
 * AnalyticsRepository - MyBatis Mapper for platform-wide analytics queries.
 * All queries target the analytics-optimized warehouse schema (dim_* and fact_* tables).
 * Focus: Big-picture platform metrics, not individual account/client analytics.
 * 
 * This repository is specifically configured to use the warehouse datasource,
 * separate from the primary transactional database.
 */
@Mapper
public interface AnalyticsRepository {

    // ============================================================
    // PLATFORM-WIDE METRICS QUERIES
    // ============================================================

    /**
     * Get comprehensive platform metrics and KPIs.
     * @return Platform-wide aggregated metrics
     */
    @Select("SELECT " +
            "COUNT(DISTINCT dc.client_id) as total_clients, " +
            "COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN dc.client_id END) as active_clients_last_30_days, " +
            "COUNT(DISTINCT da.account_id) as total_accounts, " +
            "COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN da.account_id END) as active_accounts_last_30_days, " +
            "COALESCE(SUM(fhs.total_value), 0) as total_assets_under_management, " +
            "COALESCE(AVG(fhs.total_value), 0) as average_portfolio_size, " +
            "COUNT(*) as total_orders, " +
            "COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) as total_filled_orders, " +
            "COUNT(CASE WHEN fo.status = 'CANCELLED' THEN 1 END) as total_cancelled_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as order_fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as order_cancellation_rate, " +
            "COALESCE(SUM(fo.quantity), 0) as total_volume_traded, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value_traded, " +
            "COALESCE(AVG(fo.quantity), 0) as average_order_quantity, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN fact_orders fo ON da.account_id = fo.account_id " +
            "LEFT JOIN (SELECT DISTINCT ON (account_id) account_id, total_value FROM fact_historical_snapshots ORDER BY account_id, snapshot_date DESC) fhs ON da.account_id = fhs.account_id")
    PlatformMetricsResponse getPlatformMetrics();

    // ============================================================
    // TOP INSTRUMENTS QUERIES
    // ============================================================

    /**
     * Get top traded instruments across the entire platform (by trade count).
     * @param limit Maximum number of instruments to return
     * @return List of top instruments
     */
    @Select("SELECT " +
            "di.ticker, " +
            "di.instrument_name, " +
            "di.asset_class, " +
            "di.industry, " +
            "COUNT(*) as trade_count, " +
            "SUM(fo.quantity) as total_volume, " +
            "SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)) as total_volume_value, " +
            "AVG(COALESCE(fo.filled_price, fo.limit_price)) as average_price, " +
            "di.mid_price as current_price, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' " +
            "GROUP BY di.instrument_id, di.ticker, di.instrument_name, di.asset_class, di.industry, di.mid_price " +
            "ORDER BY trade_count DESC " +
            "LIMIT #{limit}")
    List<TopInstrumentResponse> getTopTradedInstruments(@Param("limit") Integer limit);

    /**
     * Get top instruments by trading volume (in dollars).
     * @param limit Maximum number of instruments to return
     * @return List of top instruments by volume value
     */
    @Select("SELECT " +
            "di.ticker, " +
            "di.instrument_name, " +
            "di.asset_class, " +
            "di.industry, " +
            "COUNT(*) as trade_count, " +
            "SUM(fo.quantity) as total_volume, " +
            "SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)) as total_volume_value, " +
            "AVG(COALESCE(fo.filled_price, fo.limit_price)) as average_price, " +
            "di.mid_price as current_price, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' " +
            "GROUP BY di.instrument_id, di.ticker, di.instrument_name, di.asset_class, di.industry, di.mid_price " +
            "ORDER BY total_volume_value DESC " +
            "LIMIT #{limit}")
    List<TopInstrumentResponse> getTopInstrumentsByVolume(@Param("limit") Integer limit);

    // ============================================================
    // ASSET CLASS METRICS QUERIES
    // ============================================================

    /**
     * Get metrics aggregated by asset class.
     * @return List of asset class metrics
     */
    @Select("SELECT " +
            "di.asset_class, " +
            "COUNT(DISTINCT di.instrument_id) as instrument_count, " +
            "COUNT(*) as trade_count, " +
            "SUM(fo.quantity) as total_volume, " +
            "SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)) as total_volume_value, " +
            "ROUND(100.0 * SUM(fo.quantity) / NULLIF((SELECT SUM(quantity) FROM fact_orders WHERE status = 'FILLED'), 0), 2) as percent_of_total_volume, " +
            "ROUND(100.0 * SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)) / NULLIF((SELECT SUM(quantity * COALESCE(filled_price, limit_price)) FROM fact_orders WHERE status = 'FILLED'), 0), 2) as percent_of_total_value, " +
            "AVG(COALESCE(fo.filled_price, fo.limit_price)) as average_price, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' " +
            "GROUP BY di.asset_class " +
            "ORDER BY total_volume_value DESC")
    List<AssetClassMetricsResponse> getAssetClassMetrics();

    // ============================================================
    // ORDER FULFILLMENT & EXECUTION METRICS QUERIES
    // ============================================================

    /**
     * Get platform-wide order fulfillment and execution metrics.
     * @return Order metrics including fill rates, cancellation rates, and execution stats
     */
    @Select("SELECT " +
            "COUNT(*) as total_orders, " +
            "COUNT(CASE WHEN status = 'FILLED' THEN 1 END) as filled_orders, " +
            "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as cancellation_rate, " +
            "AVG(quantity) as average_order_quantity, " +
            "AVG(quantity * COALESCE(filled_price, limit_price)) as average_order_value, " +
            "SUM(quantity) as total_quantity_traded, " +
            "SUM(quantity * COALESCE(filled_price, limit_price)) as total_value_traded, " +
            "COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) as buy_order_count, " +
            "COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) as sell_order_count, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as buy_order_percent, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as sell_order_percent, " +
            "AVG(EXTRACT(EPOCH FROM (COALESCE(cancelled_at, filled_at) - created_at))) as average_order_duration_seconds " +
            "FROM fact_orders")
    OrderMetricsResponse getOrderMetrics();

    /**
     * Get order metrics filtered by date range.
     * @param startDate Start date (YYYY-MM-DD)
     * @param endDate End date (YYYY-MM-DD)
     * @return Order metrics for the specified period
     */
    @Select("SELECT " +
            "COUNT(*) as total_orders, " +
            "COUNT(CASE WHEN status = 'FILLED' THEN 1 END) as filled_orders, " +
            "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as cancellation_rate, " +
            "AVG(quantity) as average_order_quantity, " +
            "AVG(quantity * COALESCE(filled_price, limit_price)) as average_order_value, " +
            "SUM(quantity) as total_quantity_traded, " +
            "SUM(quantity * COALESCE(filled_price, limit_price)) as total_value_traded, " +
            "COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) as buy_order_count, " +
            "COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) as sell_order_count, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as buy_order_percent, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as sell_order_percent, " +
            "AVG(EXTRACT(EPOCH FROM (COALESCE(cancelled_at, filled_at) - created_at))) as average_order_duration_seconds " +
            "FROM fact_orders " +
            "WHERE DATE(created_at) BETWEEN #{startDate} AND #{endDate}")
    OrderMetricsResponse getOrderMetricsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ============================================================
    // CLIENT SEGMENTATION QUERIES (PLATFORM-WIDE)
    // ============================================================

    /**
     * Get client segmentation by risk tolerance across platform.
     * @return List of client segments by risk tolerance
     */
    @Select("SELECT " +
            "COALESCE(dc.risk_tolerance, 'UNKNOWN') as segment, " +
            "'RISK_TOLERANCE' as dimension, " +
            "COUNT(DISTINCT dc.client_id) as client_count, " +
            "COALESCE(AVG(fhs.total_value), 0) as average_portfolio_value, " +
            "COALESCE(SUM(fhs.total_value), 0) as total_assets_under_management " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN (SELECT DISTINCT ON (account_id) account_id, total_value FROM fact_historical_snapshots ORDER BY account_id, snapshot_date DESC) fhs ON da.account_id = fhs.account_id " +
            "GROUP BY dc.risk_tolerance " +
            "ORDER BY client_count DESC")
    List<ClientSegmentationResponse> getClientSegmentationByRiskTolerance();

    /**
     * Get client segmentation by portfolio size range across platform.
     * @return List of client segments by portfolio size
     */
    @Select("SELECT " +
            "COALESCE(dc.portfolio_size_range, 'UNKNOWN') as segment, " +
            "'PORTFOLIO_SIZE_RANGE' as dimension, " +
            "COUNT(DISTINCT dc.client_id) as client_count, " +
            "COALESCE(AVG(da.cash_balance), 0) as average_portfolio_value, " +
            "COALESCE(SUM(da.cash_balance), 0) as total_assets_under_management " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "GROUP BY dc.portfolio_size_range " +
            "ORDER BY client_count DESC")
    List<ClientSegmentationResponse> getClientSegmentationByPortfolioSize();

    /**
     * Get most active clients in the last 30 days by order count.
     * @param limit Maximum number of clients to return
     * @return List of most active clients
     */
    @Select("SELECT " +
            "COALESCE(dc.first_name || ' ' || dc.last_name, 'Unknown') as segment, " +
            "'ACTIVE_TRADER' as dimension, " +
            "1 as client_count, " +
            "COALESCE(SUM(fhs.total_value) / NULLIF(COUNT(DISTINCT da.account_id), 0), 0) as average_portfolio_value, " +
            "COALESCE(SUM(fhs.total_value), 0) as total_assets_under_management, " +
            "COUNT(DISTINCT fo.order_id) as total_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as order_success_rate " +
            "FROM fact_orders fo " +
            "JOIN dim_accounts da ON fo.account_id = da.account_id " +
            "JOIN dim_clients dc ON da.client_id = dc.client_id " +
            "LEFT JOIN (SELECT DISTINCT ON (account_id) account_id, total_value FROM fact_historical_snapshots ORDER BY account_id, snapshot_date DESC) fhs ON da.account_id = fhs.account_id " +
            "WHERE fo.created_at >= NOW() - INTERVAL '30 days' " +
            "GROUP BY dc.client_id, dc.first_name, dc.last_name " +
            "ORDER BY total_orders DESC " +
            "LIMIT #{limit}")
    List<ClientSegmentationResponse> getMostActiveClients(@Param("limit") Integer limit);
}
