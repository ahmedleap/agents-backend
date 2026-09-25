package com.agentsbackend.repos;

import com.agentsbackend.entities.InstrumentPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;

@Mapper
public interface InstrumentPriceRepository {

    @Select("SELECT price FROM instrument_prices WHERE instrument_id = #{instrumentId} ORDER BY as_of DESC LIMIT 1")
    Optional<BigDecimal> getLatestPrice(UUID instrumentId);
}
