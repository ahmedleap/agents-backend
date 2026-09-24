package com.agentsbackend.repos;

import com.agentsbackend.entities.Instrument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface InstrumentRepository {
    @Select("SELECT * FROM instruments WHERE instrument_id = #{instrumentId}")
    Optional<Instrument> findById(UUID instrumentId);
}
