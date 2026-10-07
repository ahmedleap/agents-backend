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
            "ROUND(100.0 * (di.mid_price - AVG(COALESCE(fo.filled_price, fo.limit_price))) / NULLIF(AVG(COALESCE(fo.filled_price, fo.limit_price)), 0), 2) as day_change_percent, " +
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
            "ROUND(100.0 * (di.mid_price - AVG(COALESCE(fo.filled_price, fo.limit_price))) / NULLIF(AVG(COALESCE(fo.filled_price, fo.limit_price)), 0), 2) as day_change_percent, " +
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
            "COALESCE(SUM(fhs.total_value), 0) as total_assets_under_management, " +
            "COALESCE(AVG(da.cash_balance), 0) as average_cash_balance, " +
            "COALESCE(COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END), 0) as total_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(fo.order_id), 0), 2) as order_success_rate, " +
            "COALESCE(COUNT(DISTINCT fo.order_id) / NULLIF(COUNT(DISTINCT dc.client_id), 0), 0) as avg_orders_per_client, " +
            "COALESCE(AVG(EXTRACT(EPOCH FROM (NOW() - da.open_date)) / 86400.0), 0) as avg_account_age " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN fact_orders fo ON da.account_id = fo.account_id " +
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
            "COALESCE(AVG(fhs.total_value), 0) as average_portfolio_value, " +
            "COALESCE(SUM(fhs.total_value), 0) as total_assets_under_management, " +
            "COALESCE(AVG(da.cash_balance), 0) as average_cash_balance, " +
            "COALESCE(COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END), 0) as total_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(fo.order_id), 0), 2) as order_success_rate, " +
            "COALESCE(COUNT(DISTINCT fo.order_id) / NULLIF(COUNT(DISTINCT dc.client_id), 0), 0) as avg_orders_per_client, " +
            "COALESCE(AVG(EXTRACT(EPOCH FROM (NOW() - da.open_date)) / 86400.0), 0) as avg_account_age " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN fact_orders fo ON da.account_id = fo.account_id " +
            "LEFT JOIN (SELECT DISTINCT ON (account_id) account_id, total_value FROM fact_historical_snapshots ORDER BY account_id, snapshot_date DESC) fhs ON da.account_id = fhs.account_id " +
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
            "COALESCE(AVG(da.cash_balance), 0) as average_cash_balance, " +
            "COUNT(DISTINCT fo.order_id) as total_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as order_success_rate, " +
            "COALESCE(COUNT(DISTINCT fo.order_id), 0) as avg_orders_per_client, " +
            "COALESCE(AVG(EXTRACT(EPOCH FROM (NOW() - da.open_date)) / 86400.0), 0) as avg_account_age " +
            "FROM fact_orders fo " +
            "JOIN dim_accounts da ON fo.account_id = da.account_id " +
            "JOIN dim_clients dc ON da.client_id = dc.client_id " +
            "LEFT JOIN (SELECT DISTINCT ON (account_id) account_id, total_value FROM fact_historical_snapshots ORDER BY account_id, snapshot_date DESC) fhs ON da.account_id = fhs.account_id " +
            "WHERE fo.created_at >= NOW() - INTERVAL '30 days' " +
            "GROUP BY dc.client_id, dc.first_name, dc.last_name " +
            "ORDER BY total_orders DESC " +
            "LIMIT #{limit}")
    List<ClientSegmentationResponse> getMostActiveClients(@Param("limit") Integer limit);

    // ============================================================
    // DASHBOARD TOP METRICS QUERIES
    // ============================================================

    /**
     * Get total volume metrics for analytics dashboard.
     * @return Total volume, value, and order count across the platform
     */
    @Select("SELECT " +
            "COALESCE(SUM(fo.quantity), 0) as total_quantity_traded, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value_traded, " +
            "COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) as total_orders_executed, " +
            "COALESCE(AVG(fo.quantity), 0) as average_order_quantity, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value, " +
            "ROUND(100.0 * COALESCE(SUM(CASE WHEN DATE(fo.created_at) = CURRENT_DATE THEN fo.quantity * COALESCE(fo.filled_price, fo.limit_price) END), 0) / " +
            "  NULLIF((SELECT COALESCE(AVG(daily_volume), 0) FROM " +
            "    (SELECT SUM(quantity * COALESCE(filled_price, limit_price)) as daily_volume " +
            "     FROM fact_orders WHERE status = 'FILLED' AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "     GROUP BY DATE(created_at)) ytd), 0) - 100.0, 2) as percent_change_ytd " +
            "FROM fact_orders fo " +
            "WHERE fo.status = 'FILLED'")
    TotalVolumeResponse getTotalVolume();

    /**
     * Get total trades metrics for analytics dashboard.
     * @return Trade count by status, fulfillment rate, and buy/sell split
     */
    @Select("SELECT " +
            "COUNT(*) as total_trades, " +
            "COUNT(CASE WHEN status = 'FILLED' THEN 1 END) as filled_trades, " +
            "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_trades, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_trades, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as cancellation_rate, " +
            "COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) as buy_order_count, " +
            "COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) as sell_order_count, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as buy_percentage, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as sell_percentage " +
            "FROM fact_orders")
    TotalTradesResponse getTotalTrades();

    /**
     * Get active clients metrics for analytics dashboard.
     * @return Count of active clients and accounts
     */
    @Select("SELECT " +
            "COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN dc.client_id END) as total_active_clients, " +
            "COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN dc.client_id END) as active_clients_last_30_days, " +
            "COUNT(DISTINCT dc.client_id) as total_clients, " +
            "ROUND(100.0 * COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN dc.client_id END) / NULLIF(COUNT(DISTINCT dc.client_id), 0), 2) as active_client_percentage, " +
            "COUNT(DISTINCT CASE WHEN fo.created_at >= NOW() - INTERVAL '30 days' THEN da.account_id END) as active_accounts, " +
            "COUNT(DISTINCT da.account_id) as total_accounts, " +
            "0 as new_clients_today " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN fact_orders fo ON da.account_id = fo.account_id")
    ActiveClientsResponse getActiveClients();

    // ============================================================
    // DAILY METRICS QUERIES (For specific date)
    // ============================================================

    /**
     * Get total volume metrics for a specific date.
     * @param date The date to get metrics for
     * @return Total volume for the day
     */
    @Select("SELECT " +
            "COALESCE(SUM(fo.quantity), 0) as total_quantity_traded, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value_traded, " +
            "COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) as total_orders_executed, " +
            "COALESCE(AVG(fo.quantity), 0) as average_order_quantity, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value, " +
            "ROUND(100.0 * COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) / " +
            "  NULLIF((SELECT COALESCE(AVG(daily_volume), 0) FROM " +
            "    (SELECT SUM(quantity * COALESCE(filled_price, limit_price)) as daily_volume " +
            "     FROM fact_orders WHERE status = 'FILLED' AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM #{date}) " +
            "     GROUP BY DATE(created_at)) ytd), 0) - 100.0, 2) as percent_change_ytd " +
            "FROM fact_orders fo " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) = #{date}")
    TotalVolumeResponse getDailyTotalVolume(@Param("date") LocalDate date);

    /**
     * Get total trades metrics for a specific date.
     * @param date The date to get metrics for
     * @return Trade metrics for the day
     */
    @Select("SELECT " +
            "COUNT(*) as total_trades, " +
            "COUNT(CASE WHEN status = 'FILLED' THEN 1 END) as filled_trades, " +
            "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_trades, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_trades, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as cancellation_rate, " +
            "COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) as buy_order_count, " +
            "COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) as sell_order_count, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'BUY' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as buy_percentage, " +
            "ROUND(100.0 * COUNT(CASE WHEN order_type = 'SELL' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as sell_percentage " +
            "FROM fact_orders " +
            "WHERE DATE(created_at) = #{date}")
    TotalTradesResponse getDailyTotalTrades(@Param("date") LocalDate date);

    /**
     * Get active clients metrics for a specific date.
     * @param date The date to get metrics for
     * @return Active clients count for the day
     */
    @Select("SELECT " +
            "COUNT(DISTINCT CASE WHEN DATE(fo.created_at) = #{date} THEN dc.client_id END) as total_active_clients, " +
            "COUNT(DISTINCT dc.client_id) as active_clients_last_30_days, " +
            "COUNT(DISTINCT dc.client_id) as total_clients, " +
            "ROUND(100.0 * COUNT(DISTINCT CASE WHEN DATE(fo.created_at) = #{date} THEN dc.client_id END) / NULLIF(COUNT(DISTINCT dc.client_id), 0), 2) as active_client_percentage, " +
            "COUNT(DISTINCT CASE WHEN DATE(fo.created_at) = #{date} THEN da.account_id END) as active_accounts, " +
            "COUNT(DISTINCT da.account_id) as total_accounts, " +
            "COALESCE(COUNT(DISTINCT CASE WHEN DATE(fo.created_at) = #{date} AND fo.created_at = (SELECT MIN(created_at) FROM fact_orders WHERE account_id = fo.account_id) THEN dc.client_id END), 0) as new_clients_today " +
            "FROM dim_clients dc " +
            "LEFT JOIN dim_accounts da ON dc.client_id = da.client_id " +
            "LEFT JOIN fact_orders fo ON da.account_id = fo.account_id")
    ActiveClientsResponse getDailyActiveClients(@Param("date") LocalDate date);

    // ============================================================
    // TREND QUERIES (For time series analysis)
    // ============================================================

    /**
     * Get trading volume trend over a date range (daily aggregation).
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily volume data points
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "COALESCE(SUM(fo.quantity), 0) as total_quantity_traded, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value_traded, " +
            "COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) as trade_count, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value, " +
            "COUNT(CASE WHEN fo.order_type = 'BUY' THEN 1 END) as buy_count, " +
            "COUNT(CASE WHEN fo.order_type = 'SELL' THEN 1 END) as sell_count " +
            "FROM fact_orders fo " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at) " +
            "ORDER BY DATE(fo.created_at) ASC")
    List<VolumeTrendResponse> getVolumeTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Get top instruments trading trend over a date range.
     * @param startDate Start date
     * @param endDate End date
     * @param limit Number of top instruments to include
     * @return List of instrument trading trends
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "di.ticker, " +
            "di.instrument_name, " +
            "di.asset_class, " +
            "COUNT(*) as trade_count, " +
            "COALESCE(SUM(fo.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "COALESCE(AVG(COALESCE(fo.filled_price, fo.limit_price)), 0) as average_price, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at), di.instrument_id, di.ticker, di.instrument_name, di.asset_class " +
            "ORDER BY DATE(fo.created_at) DESC, total_value DESC")
    List<InstrumentTrendResponse> getInstrumentTrendByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Get top N instruments for a specific date.
     * @param date The date to analyze
     * @param limit Number of top instruments
     * @return List of top instruments for that day
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "di.ticker, " +
            "di.instrument_name, " +
            "di.asset_class, " +
            "COUNT(*) as trade_count, " +
            "COALESCE(SUM(fo.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "COALESCE(AVG(COALESCE(fo.filled_price, fo.limit_price)), 0) as average_price, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) = #{date} " +
            "GROUP BY di.instrument_id, di.ticker, di.instrument_name, di.asset_class " +
            "ORDER BY total_value DESC " +
            "LIMIT #{limit}")
    List<InstrumentTrendResponse> getTopInstrumentsForDate(@Param("date") LocalDate date, @Param("limit") Integer limit);

    // ============================================================
    // PRIORITY 1: CLIENT ACTIVITY TREND QUERIES
    // ============================================================

    /**
     * Get client activity trends over a date range.
     * Shows trading activity by individual clients over time.
     * @param startDate Start date
     * @param endDate End date
     * @param limit Maximum number of top clients per day
     * @return List of client activity trends
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "dc.client_id, " +
            "dc.first_name || ' ' || dc.last_name as client_name, " +
            "COUNT(*) as trade_count, " +
            "COALESCE(SUM(fo.quantity), 0) as total_volume, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as order_success_rate " +
            "FROM fact_orders fo " +
            "JOIN dim_accounts da ON fo.account_id = da.account_id " +
            "JOIN dim_clients dc ON da.client_id = dc.client_id " +
            "WHERE DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at), dc.client_id, dc.first_name, dc.last_name " +
            "ORDER BY DATE(fo.created_at) DESC, trade_count DESC " +
            "LIMIT #{limit}")
    List<ClientActivityTrendResponse> getClientActivityTrend(@Param("startDate") LocalDate startDate, 
                                                              @Param("endDate") LocalDate endDate,
                                                              @Param("limit") Integer limit);

    // ============================================================
    // PRIORITY 2: INSTRUMENT ANALYSIS QUERIES
    // ============================================================

    /**
     * Get detailed analysis for a specific instrument over a date range.
     * @param ticker Instrument ticker symbol
     * @param startDate Start date
     * @param endDate End date
     * @return Instrument analysis with trading metrics
     */
    @Select("SELECT " +
            "di.ticker, " +
            "di.instrument_name, " +
            "di.asset_class, " +
            "COALESCE(SUM(fo.quantity), 0) as total_volume, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "COUNT(*) as trade_count, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders, " +
            "COALESCE(AVG(COALESCE(fo.filled_price, fo.limit_price)), 0) as average_price, " +
            "0 as volume_trend, " +
            "ROUND(100.0 * SUM(CASE WHEN fo.order_type = 'BUY' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 2) as buy_vs_sell_ratio " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE di.ticker = #{ticker} AND fo.status = 'FILLED' AND DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY di.instrument_id, di.ticker, di.instrument_name, di.asset_class")
    InstrumentAnalysisResponse getInstrumentAnalysis(@Param("ticker") String ticker,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // ============================================================
    // PRIORITY 3: CLIENT SEGMENT TREND QUERIES
    // ============================================================

    /**
     * Get client segment trends by risk tolerance or portfolio size.
     * @param segmentType RISK_TOLERANCE or PORTFOLIO_SIZE
     * @param startDate Start date
     * @param endDate End date
     * @return List of segment trends over time
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "CASE WHEN #{segmentType} = 'RISK_TOLERANCE' THEN COALESCE(dc.risk_tolerance, 'UNKNOWN') " +
            "     WHEN #{segmentType} = 'PORTFOLIO_SIZE' THEN COALESCE(dc.portfolio_size_range, 'UNKNOWN') " +
            "     ELSE 'UNKNOWN' END as segment, " +
            "COUNT(*) as trade_count, " +
            "COALESCE(SUM(fo.quantity), 0) as total_volume, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "COUNT(DISTINCT dc.client_id) as active_client_count, " +
            "COALESCE(AVG(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as average_order_value " +
            "FROM fact_orders fo " +
            "JOIN dim_accounts da ON fo.account_id = da.account_id " +
            "JOIN dim_clients dc ON da.client_id = dc.client_id " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at), " +
            "CASE WHEN #{segmentType} = 'RISK_TOLERANCE' THEN COALESCE(dc.risk_tolerance, 'UNKNOWN') " +
            "     WHEN #{segmentType} = 'PORTFOLIO_SIZE' THEN COALESCE(dc.portfolio_size_range, 'UNKNOWN') " +
            "     ELSE 'UNKNOWN' END " +
            "ORDER BY DATE(fo.created_at) ASC")
    List<SegmentTrendResponse> getSegmentTrend(@Param("segmentType") String segmentType,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // ============================================================
    // PRIORITY 4: FULFILLMENT TREND QUERIES
    // ============================================================

    /**
     * Get order fulfillment trends over a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily fulfillment metrics
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "COUNT(*) as total_orders, " +
            "COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) as filled_orders, " +
            "COUNT(CASE WHEN fo.status = 'CANCELLED' THEN 1 END) as cancelled_orders, " +
            "COUNT(CASE WHEN fo.status = 'PENDING' THEN 1 END) as pending_orders, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'FILLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as fulfillment_rate, " +
            "ROUND(100.0 * COUNT(CASE WHEN fo.status = 'CANCELLED' THEN 1 END) / NULLIF(COUNT(*), 0), 2) as cancellation_rate, " +
            "0 as average_order_duration_seconds, " +
            "NULL as peak_trading_time " +
            "FROM fact_orders fo " +
            "WHERE DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at) " +
            "ORDER BY DATE(fo.created_at) ASC")
    List<FulfillmentTrendResponse> getFulfillmentTrend(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // ============================================================
    // PRIORITY 5: ASSET CLASS TREND QUERIES
    // ============================================================

    /**
     * Get asset class trading trends over a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of asset class trends over time
     */
    @Select("SELECT " +
            "DATE(fo.created_at) as date, " +
            "di.asset_class, " +
            "COUNT(*) as trade_count, " +
            "COALESCE(SUM(fo.quantity), 0) as total_volume, " +
            "COALESCE(SUM(fo.quantity * COALESCE(fo.filled_price, fo.limit_price)), 0) as total_value, " +
            "ROUND(100.0 * SUM(fo.quantity) / (SELECT COALESCE(SUM(quantity), 1) FROM fact_orders WHERE DATE(created_at) = DATE(fo.created_at) AND status = 'FILLED'), 2) as percent_of_total_volume, " +
            "COUNT(DISTINCT fo.account_id) as unique_traders " +
            "FROM fact_orders fo " +
            "JOIN dim_instruments di ON fo.instrument_id = di.instrument_id " +
            "WHERE fo.status = 'FILLED' AND DATE(fo.created_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(fo.created_at), di.asset_class " +
            "ORDER BY DATE(fo.created_at) ASC, total_value DESC")
    List<AssetClassTrendResponse> getAssetClassTrend(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
