package com.agentsbackend.repos;

import com.agentsbackend.enums.TransactionType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Mapper
public interface CashManagementRepository {

    @Select("SELECT cash_balance FROM accounts WHERE account_id = #{accountId}")
    BigDecimal getCashBalance(@Param("accountId") UUID accountId);

    @Update("UPDATE accounts SET cash_balance = cash_balance + #{delta} WHERE account_id = #{accountId}")
    int updateCashBalance(@Param("accountId") UUID accountId, @Param("delta") BigDecimal delta);

    @Insert("INSERT INTO transactions (transaction_id, account_id, txn_type, amount, created_at) " +
            "VALUES (#{transactionId}, #{accountId}, CAST(#{txnType} AS transaction_type), #{amount}, #{createdAt})")
    int insertTransaction(@Param("transactionId") UUID transactionId,
                         @Param("accountId") UUID accountId,
                         @Param("txnType") TransactionType txnType,
                         @Param("amount") BigDecimal amount,
                         @Param("createdAt") LocalDateTime createdAt);
}
