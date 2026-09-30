package com.agentsbackend.repos;

import com.agentsbackend.entities.Instrument;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis Mapper for Instrument entity.
 * Handles all database operations for instruments including CRUD operations
 * and retrieval of current pricing (bid, ask, mid_price).
 */
@Mapper
public interface InstrumentRepository {

    /**
     * Find an instrument by its ID.
     * Returns current pricing data (bid, ask, mid_price).
     *
     * @param instrumentId UUID of the instrument
     * @return Optional containing the Instrument if found
     */
    @Select("SELECT instrument_id, ticker, asset_class, bid, ask, mid_price, price_updated_at " +
            "FROM instruments WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR}")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "ticker", column = "ticker"),
        @Result(property = "assetClass", column = "asset_class"),
        @Result(property = "bid", column = "bid"),
        @Result(property = "ask", column = "ask"),
        @Result(property = "midPrice", column = "mid_price"),
        @Result(property = "priceUpdatedAt", column = "price_updated_at")
    })
    Optional<Instrument> findById(UUID instrumentId);

    /**
     * Find all instruments.
     * Returns all instruments with current pricing data.
     *
     * @return List of all instruments
     */
    @Select("SELECT instrument_id, ticker, asset_class, bid, ask, mid_price, price_updated_at " +
            "FROM instruments ORDER BY ticker")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "ticker", column = "ticker"),
        @Result(property = "assetClass", column = "asset_class"),
        @Result(property = "bid", column = "bid"),
        @Result(property = "ask", column = "ask"),
        @Result(property = "midPrice", column = "mid_price"),
        @Result(property = "priceUpdatedAt", column = "price_updated_at")
    })
    List<Instrument> findAll();

    /**
     * Find an instrument by ticker symbol.
     *
     * @param ticker The ticker symbol
     * @return Optional containing the Instrument if found
     */
    @Select("SELECT instrument_id, ticker, asset_class, bid, ask, mid_price, price_updated_at " +
            "FROM instruments WHERE ticker = #{ticker,jdbcType=VARCHAR}")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "ticker", column = "ticker"),
        @Result(property = "assetClass", column = "asset_class"),
        @Result(property = "bid", column = "bid"),
        @Result(property = "ask", column = "ask"),
        @Result(property = "midPrice", column = "mid_price"),
        @Result(property = "priceUpdatedAt", column = "price_updated_at")
    })
    Optional<Instrument> findByTicker(String ticker);

    /**
     * Update instrument pricing (bid, ask, mid_price, price_updated_at).
     * Called when new market data is received.
     *
     * @param instrument Instrument with updated pricing
     */
    @Update("UPDATE instruments SET bid = #{bid,jdbcType=NUMERIC}, ask = #{ask,jdbcType=NUMERIC}, " +
            "mid_price = #{midPrice,jdbcType=NUMERIC}, price_updated_at = #{priceUpdatedAt} " +
            "WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR}")
    void updatePricing(Instrument instrument);
}
