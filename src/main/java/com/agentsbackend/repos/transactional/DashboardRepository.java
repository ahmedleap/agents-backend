package com.agentsbackend.repos.transactional;

import com.agentsbackend.DTO.response.DashboardResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DashboardRepository {

    @Select("SELECT o.order_id, o.account_id, a.name AS account_name, i.ticker, " +
            "i.name AS instrument_name, o.order_type, o.quantity, o.limit_price, " +
            "o.filled_price, o.status, o.created_at " +
            "FROM orders o JOIN accounts a ON a.account_id = o.account_id " +
            "JOIN instruments i ON i.instrument_id = o.instrument_id " +
            "WHERE a.client_id = #{clientId} " +
            "ORDER BY o.created_at DESC LIMIT 5")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "accountName", column = "account_name"),
            @Result(property = "ticker", column = "ticker"),
            @Result(property = "instrumentName", column = "instrument_name"),
            @Result(property = "orderType", column = "order_type"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "limitPrice", column = "limit_price"),
            @Result(property = "filledPrice", column = "filled_price"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<DashboardResponse.RecentTrade> findRecentTrades(@Param("clientId") UUID clientId);

    @Select("SELECT snapshot_date AS date, COALESCE(SUM(total_value), 0) AS total_value " +
            "FROM historical_snapshot hs JOIN accounts a ON a.account_id = hs.account_id " +
            "WHERE a.client_id = #{clientId} AND a.status = 'ACTIVE' " +
            "AND snapshot_date >= #{startDate} " +
            "GROUP BY snapshot_date ORDER BY snapshot_date ASC")
    @Results({
            @Result(property = "date", column = "date"),
            @Result(property = "totalValue", column = "total_value")
    })
    List<DashboardResponse.PortfolioHistoryPoint> findPortfolioHistory(
            @Param("clientId") UUID clientId, @Param("startDate") LocalDate startDate);

    @Select("SELECT COALESCE(SUM(hs.total_value), 0) " +
            "FROM historical_snapshot hs JOIN accounts a ON a.account_id = hs.account_id " +
            "WHERE a.client_id = #{clientId} AND a.status = 'ACTIVE' " +
            "AND hs.snapshot_date = (" +
            "SELECT MAX(previous.snapshot_date) FROM historical_snapshot previous " +
            "WHERE previous.account_id = hs.account_id AND previous.snapshot_date < #{asOfDate})")
    BigDecimal findPreviousPortfolioValue(
            @Param("clientId") UUID clientId, @Param("asOfDate") LocalDate asOfDate);

    @Select("WITH ranked_prices AS (" +
            "SELECT instrument_id, close, ROW_NUMBER() OVER (PARTITION BY instrument_id ORDER BY timestamp DESC) AS position " +
            "FROM instrument_price_history) " +
            "SELECT i.ticker, i.name AS instrument_name, latest.close AS current_price, " +
            "latest.close - previous.close AS price_change, " +
            "ROUND(100 * (latest.close - previous.close) / NULLIF(previous.close, 0), 2) AS price_change_percent " +
            "FROM ranked_prices latest JOIN ranked_prices previous ON previous.instrument_id = latest.instrument_id " +
            "AND previous.position = 2 JOIN instruments i ON i.instrument_id = latest.instrument_id " +
            "WHERE latest.position = 1 ORDER BY ABS((latest.close - previous.close) / NULLIF(previous.close, 0)) DESC " +
            "LIMIT #{limit}")
    @Results({
            @Result(property = "ticker", column = "ticker"),
            @Result(property = "instrumentName", column = "instrument_name"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "priceChange", column = "price_change"),
            @Result(property = "priceChangePercent", column = "price_change_percent")
    })
    List<DashboardResponse.MarketMover> findMarketMovers(@Param("limit") int limit);
}