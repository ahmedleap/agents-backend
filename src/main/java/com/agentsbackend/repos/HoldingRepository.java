package com.agentsbackend.repos;

import com.agentsbackend.entities.Holding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;
import java.util.Optional;

@Mapper
public interface HoldingRepository {
    
    @Select("SELECT * FROM holdings WHERE account_id = #{accountId}")
    @Results({
        @Result(property = "holdingId", column = "holding_id"),
        @Result(property = "account", column = "account_id", 
            one = @One(select = "com.agentsbackend.repos.AccountRepository.findById")),
        @Result(property = "instrument", column = "instrument_id",
            one = @One(select = "com.agentsbackend.repos.InstrumentRepository.findById"))
    })
    java.util.List<Holding> findByAccountId(UUID accountId);

    @Select("SELECT * FROM holdings WHERE holding_id = #{holdingId} AND account_id = #{accountId}")
    @Results({
        @Result(property = "account", column = "account_id", one = @One(select = "com.agentsbackend.repos.AccountRepository.findById")),
        @Result(property = "instrument", column = "instrument_id", one = @One(select = "com.agentsbackend.repos.InstrumentRepository.findById"))
    })
    Optional<Holding> findOneHolding(@Param("holdingId") UUID holdingId, @Param("accountId") UUID accountId);
}
