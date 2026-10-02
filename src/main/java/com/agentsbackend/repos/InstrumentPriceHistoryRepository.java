package com.agentsbackend.repos;

import com.agentsbackend.entities.InstrumentPriceHistory;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface InstrumentPriceHistoryRepository {

    /**
     * Find the latest price history record for an instrument.
     *
     * @param instrumentId UUID of the instrument
     * @return Optional containing the most recent InstrumentPriceHistory
     */
    @Select("SELECT price_history_id, instrument_id, timestamp, open, high, low, close, volume " +
            "FROM instrument_price_history WHERE instrument_id = #{instrumentId} " +
            "ORDER BY timestamp DESC LIMIT 1")
    @Results({
        @Result(property = "priceHistoryId", column = "price_history_id"),
        @Result(property = "instrument.instrumentId", column = "instrument_id"),
        @Result(property = "timestamp", column = "timestamp"),
        @Result(property = "open", column = "open"),
        @Result(property = "high", column = "high"),
        @Result(property = "low", column = "low"),
        @Result(property = "close", column = "close"),
        @Result(property = "volume", column = "volume")
    })
    Optional<InstrumentPriceHistory> findLatestPrice(@Param("instrumentId") UUID instrumentId);

    /**
     * Find all price history records for an instrument within a date range.
     *
     * @param instrumentId UUID of the instrument
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return List of InstrumentPriceHistory records
     */
    @Select("SELECT price_history_id, instrument_id, timestamp, open, high, low, close, volume " +
            "FROM instrument_price_history " +
            "WHERE instrument_id = #{instrumentId} AND timestamp >= #{startDate} AND timestamp <= #{endDate} " +
            "ORDER BY timestamp DESC")
    @Results({
        @Result(property = "priceHistoryId", column = "price_history_id"),
        @Result(property = "instrument.instrumentId", column = "instrument_id"),
        @Result(property = "timestamp", column = "timestamp"),
        @Result(property = "open", column = "open"),
        @Result(property = "high", column = "high"),
        @Result(property = "low", column = "low"),
        @Result(property = "close", column = "close"),
        @Result(property = "volume", column = "volume")
    })
    List<InstrumentPriceHistory> findByInstrumentAndDateRange(
        @Param("instrumentId") UUID instrumentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find the last N price history records for an instrument.
     *
     * @param instrumentId UUID of the instrument
     * @param limit Number of records to return
     * @return List of InstrumentPriceHistory records
     */
    @Select("SELECT price_history_id, instrument_id, timestamp, open, high, low, close, volume " +
            "FROM instrument_price_history WHERE instrument_id = #{instrumentId} " +
            "ORDER BY timestamp DESC LIMIT #{limit}")
    @Results({
        @Result(property = "priceHistoryId", column = "price_history_id"),
        @Result(property = "instrument.instrumentId", column = "instrument_id"),
        @Result(property = "timestamp", column = "timestamp"),
        @Result(property = "open", column = "open"),
        @Result(property = "high", column = "high"),
        @Result(property = "low", column = "low"),
        @Result(property = "close", column = "close"),
        @Result(property = "volume", column = "volume")
    })
    List<InstrumentPriceHistory> findLatestPrices(@Param("instrumentId") UUID instrumentId, @Param("limit") int limit);

    /**
     * Insert a new price history record.
     *
     * @param priceHistory InstrumentPriceHistory to save
     */
    @Insert("INSERT INTO instrument_price_history (price_history_id, instrument_id, timestamp, open, high, low, close, volume) " +
            "VALUES (#{priceHistoryId}, #{instrument.instrumentId}, #{timestamp}, #{open}, #{high}, #{low}, #{close}, #{volume})")
    void save(InstrumentPriceHistory priceHistory);

    /**
     * Find a price history record by ID.
     *
     * @param priceHistoryId UUID of the price history record
     * @return Optional containing the InstrumentPriceHistory if found
     */
    @Select("SELECT price_history_id, instrument_id, timestamp, open, high, low, close, volume " +
            "FROM instrument_price_history WHERE price_history_id = #{priceHistoryId}")
    @Results({
        @Result(property = "priceHistoryId", column = "price_history_id"),
        @Result(property = "instrument.instrumentId", column = "instrument_id"),
        @Result(property = "timestamp", column = "timestamp"),
        @Result(property = "open", column = "open"),
        @Result(property = "high", column = "high"),
        @Result(property = "low", column = "low"),
        @Result(property = "close", column = "close"),
        @Result(property = "volume", column = "volume")
    })
    Optional<InstrumentPriceHistory> findById(@Param("priceHistoryId") UUID priceHistoryId);
}
