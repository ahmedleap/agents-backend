package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Transaction;
import java.util.List;
import java.util.UUID;

@Mapper
public interface TransactionRepository {

    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at FROM transactions WHERE account_id = #{accountId} ORDER BY created_at DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at FROM transactions WHERE transaction_id = #{transactionId}")
    Transaction findById(@Param("transactionId") UUID transactionId);

}
