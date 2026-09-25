package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Account;
import java.util.UUID;

@Mapper
public interface AccountRepository {
    
    @Select("SELECT * FROM accounts WHERE account_id = #{accountId}")
    Account findById(@Param("accountId") UUID accountId);
    
    @Update("UPDATE accounts SET cash_balance = #{cashBalance} WHERE account_id = #{accountId}")
    void save(Account account);
}
