package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AccountRepository {
    @Select("SELECT * FROM accounts WHERE account_id = #{accountId}")
    Optional<Account> findById(UUID accountId);
}
