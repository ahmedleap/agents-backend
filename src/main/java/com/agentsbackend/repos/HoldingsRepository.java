package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Holding;
import java.util.UUID;
import java.util.Optional;

@Mapper
public interface HoldingsRepository {
    
    @Select("SELECT * FROM holdings WHERE account_id = #{accountId} AND instrument_id = #{instrumentId}")
    Holding findByAccountAndInstrument(@Param("accountId") UUID accountId, @Param("instrumentId") UUID instrumentId);
}
