package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;

@Mapper
public interface InstrumentPriceRepository {

    @Select("SELECT close FROM instrument_price_history WHERE instrument_id = #{instrumentId} ORDER BY timestamp DESC LIMIT 1")
    Optional<BigDecimal> getLatestPrice(UUID instrumentId);
}
