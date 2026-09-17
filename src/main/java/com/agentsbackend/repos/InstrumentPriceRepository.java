package com.agentsbackend.repos;

import com.agentsbackend.entities.InstrumentPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface InstrumentPriceRepository {

    @Insert("INSERT INTO instrument_prices (price_id, instrument_id, price, as_of) " +
            "VALUES (#{priceId}, #{instrumentId}, #{price}, #{asOf})")
    void createInstrumentPrice(InstrumentPrice price);

    @Select("SELECT * FROM instrument_prices WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR} " +
            "ORDER BY as_of DESC LIMIT 1")
    Optional<InstrumentPrice> findLatestPrice(@Param("instrumentId") UUID instrumentId);

    // Find the price of an instrument as of a specific date and time, can be used for EOD valuations
    @Select("SELECT * FROM instrument_prices WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR} " +
            "AND as_of = #{asOf}")
    Optional<InstrumentPrice> findPriceAsOf(@Param("instrumentId") UUID instrumentId, 
                                             @Param("asOf") Instant asOf);

    // Shows complete price history for a given instrument
    @Select("SELECT * FROM instrument_prices WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR} " +
            "ORDER BY as_of DESC")
    List<InstrumentPrice> findAllPricesForInstrument(@Param("instrumentId") UUID instrumentId);
}
