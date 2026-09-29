package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AccountRepository {
    @Select("SELECT * FROM accounts WHERE account_id = #{accountId}")
    @Results({
        @Result(property = "client", column = "client_id", one = @One(select = "com.agentsbackend.repos.ClientRepository.findById"))
    })
    Optional<Account> findById(UUID accountId);

    @Select("SELECT * FROM accounts WHERE client_id = #{clientId}")
    @Results({
        @Result(property = "client", column = "client_id", one = @One(select = "com.agentsbackend.repos.ClientRepository.findById"))
    })
    java.util.List<Account> findByClientId(UUID clientId);
}
