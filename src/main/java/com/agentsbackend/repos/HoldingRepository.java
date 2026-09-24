package com.agentsbackend.repos;

import com.agentsbackend.entities.Holding;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface HoldingRepository {
    
    @Insert("INSERT INTO holdings (holding_id, account_id, instrument_id, quantity, average_cost_basis) " +
        "VALUES (#{holdingId,jdbcType=VARCHAR}, #{account.accountId,jdbcType=VARCHAR}, #{instrument.instrumentId,jdbcType=VARCHAR}, #{quantity}, #{averageCostBasis})")
    void createHolding(Holding holding);
}
