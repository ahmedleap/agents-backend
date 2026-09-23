package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.InstrumentPrice;
import java.util.UUID;

@Mapper
public interface InstrumentPriceRepository {
    
    @Select("SELECT * FROM instrument_prices WHERE instrument_id = #{instrumentId} ORDER BY as_of DESC LIMIT 1")
    InstrumentPrice findLatestPrice(@Param("instrumentId") UUID instrumentId);
}
