package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.One;

import java.util.UUID;
import java.util.Optional;

@Mapper
public interface HoldingRepository {
    
    @Insert("INSERT INTO holdings (holding_id, account_id, instrument_id, quantity, average_cost_basis) " +
        "VALUES (#{holdingId,jdbcType=VARCHAR}, #{account.accountId,jdbcType=VARCHAR}, #{instrument.instrumentId,jdbcType=VARCHAR}, #{quantity}, #{averageCostBasis})")
    void createHolding(Holding holding);

    @Select("SELECT * FROM holdings WHERE account_id = #{accountId}")
    @Results({
        @Result(property = "holdingId", column = "holding_id"),
        @Result(property = "account", column = "account_id", 
            one = @One(select = "com.agentsbackend.repos.AccountRepository.findById")),
        @Result(property = "instrument", column = "instrument_id",
            one = @One(select = "com.agentsbackend.repos.InstrumentRepository.findById"))
    })
    java.util.List<Holding> findByAccountId(UUID accountId);
}
