package com.agentsbackend.repos;

import com.agentsbackend.entities.Instrument;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis Mapper for Instrument entity and market data.
 * Handles all database operations for instruments including search,
 * pricing updates, and historical price queries.
 */
@Mapper
public interface InstrumentRepository {

    /**
     * Search instruments by ticker or name (case-insensitive partial match).
     * Used by GET /instruments/search?q=
     *
     * @param query Search query (minimum 1 character, maximum 50)
     * @return List of Instrument objects matching the query
     */
    @Select("SELECT instrument_id, ticker, name, asset_class, industry, bid, ask, price_updated_at " +
            "FROM instruments " +
            "WHERE LOWER(ticker) LIKE LOWER(CONCAT('%', #{query}, '%')) " +
            "   OR LOWER(name) LIKE LOWER(CONCAT('%', #{query}, '%')) " +
            "ORDER BY ticker ASC " +
            "LIMIT 50")
    List<Instrument> searchByQuery(@Param("query") String query);

    /**
     * Find instrument by ticker symbol.
     * Used by GET /instruments/{ticker} and GET /instruments/{ticker}/quote
     *
     * @param ticker Stock ticker symbol (e.g., "AAPL")
     * @return Optional containing Instrument if found, empty otherwise
     */
    @Select("SELECT instrument_id, ticker, name, asset_class, industry, bid, ask, price_updated_at " +
            "FROM instruments WHERE ticker = #{ticker}")
    Optional<Instrument> findByTicker(@Param("ticker") String ticker);

    /**
     * Update instrument with latest market pricing from Alpaca API.
     * Updates bid, ask, and price_updated_at columns.
     *
     * @param instrument Instrument with updated bid, ask, and price_updated_at
     */
    @Update("UPDATE instruments " +
            "SET bid = #{bid}, ask = #{ask}, price_updated_at = #{priceUpdatedAt} " +
            "WHERE instrument_id = #{instrumentId}")
    void updateInstrumentPricing(Instrument instrument);

    /**
     * Find historical OHLCV price data for an instrument within a date range.
     * Used by GET /instruments/{ticker}/history?period=
     * Results ordered by timestamp DESC (most recent first).
     *
     * @param instrumentId UUID of the instrument
     * @param startTime Start of the date range (inclusive)
     * @param endTime End of the date range (inclusive)
     * @return List of price history records ordered by timestamp DESC
     */
    @Select("SELECT price_history_id, instrument_id, timestamp, open, high, low, close, volume " +
            "FROM instrument_price_history " +
            "WHERE instrument_id = #{instrumentId} " +
            "  AND timestamp >= #{startTime} " +
            "  AND timestamp <= #{endTime} " +
            "ORDER BY timestamp DESC")
    List<Instrument> findHistoricalPrices(
        @Param("instrumentId") UUID instrumentId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
