package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper
public interface TransactionRepository {

    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at FROM transactions WHERE account_id = #{accountId} ORDER BY created_at DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at FROM transactions WHERE transaction_id = #{transactionId}")
    Transaction findById(@Param("transactionId") UUID transactionId);

    /**
     * Record a cash transaction (deposit or withdrawal).
     * Inserts transaction record into transactions table with auto-generated UUID.
     *
     * @param accountId UUID of the account
     * @param amount Amount of the transaction
     * @param transactionType "DEPOSIT" or "WITHDRAWAL"
     */
    @Insert("INSERT INTO transactions (transaction_id, account_id, txn_type, amount) " +
            "VALUES (gen_random_uuid(), #{accountId,jdbcType=VARCHAR}, " +
            "CAST(#{transactionType} AS transaction_type), #{amount,jdbcType=NUMERIC})")
    void recordTransaction(@Param("accountId") UUID accountId, @Param("amount") BigDecimal amount,
                          @Param("transactionType") String transactionType);

    /**
     * Get count of transactions for an account.
     * Transactions represent cash deposits and withdrawals.
     *
     * @param accountId UUID of the account
     * @return Count of transactions, or null if none
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    Integer getTransactionCount(@Param("accountId") UUID accountId);

}
