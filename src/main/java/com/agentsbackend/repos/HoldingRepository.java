package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;


import java.util.UUID;
import java.util.Optional;
import java.util.List;

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
    List<Holding> findByAccountId(UUID accountId);

    @Select("SELECT * FROM holdings WHERE holding_id = #{holdingId} AND account_id = #{accountId}")
    @Results({
        @Result(property = "account", column = "account_id", one = @One(select = "com.agentsbackend.repos.AccountRepository.findById")),
        @Result(property = "instrument", column = "instrument_id", one = @One(select = "com.agentsbackend.repos.InstrumentRepository.findById"))
    })
    Optional<Holding> findOneHolding(@Param("holdingId") UUID holdingId, @Param("accountId") UUID accountId);

    @Select("SELECT * FROM accounts WHERE client_id = #{clientId}")
    List<Account> findByClientId(UUID clientId);

    @Select("SELECT * FROM holdings WHERE account_id = #{accountId} AND instrument_id = #{instrumentId}")
    Holding findByAccountAndInstrument(@Param("accountId") UUID accountId, @Param("instrumentId") UUID instrumentId);

    @Select("SELECT * FROM holdings WHERE account_id = #{accountId}")
    List<Holding> findAllByAccount(@Param("accountId") UUID accountId);
    
    @Insert("INSERT INTO holdings (holding_id, account_id, instrument_id, quantity, average_cost_basis) " +
            "VALUES (#{holdingId}, #{account.accountId}, #{instrument.instrumentId}, #{quantity}, #{averageCostBasis}) " +
            "ON CONFLICT (account_id, instrument_id) DO UPDATE SET quantity = #{quantity}, average_cost_basis = #{averageCostBasis}")
    void save(Holding holding);
    
    @Update("UPDATE holdings SET quantity = #{quantity}, average_cost_basis = #{averageCostBasis} " +
            "WHERE account_id = #{account.accountId} AND instrument_id = #{instrument.instrumentId}")
    void update(Holding holding);
    
    @Delete("DELETE FROM holdings WHERE account_id = #{accountId} AND instrument_id = #{instrumentId}")
    void deleteByAccountAndInstrument(@Param("accountId") UUID accountId, @Param("instrumentId") UUID instrumentId);
}
