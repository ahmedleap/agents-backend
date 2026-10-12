package com.agentsbackend.repos;

import com.agentsbackend.entities.InstrumentPriceHistory;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MyBatis Mapper for InstrumentPriceHistory entity.
 * Handles all database operations for historical OHLCV price data.
 */
@Mapper
public interface InstrumentPriceHistoryRepository {

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
    List<InstrumentPriceHistory> findHistoricalPrices(
        @Param("instrumentId") UUID instrumentId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
