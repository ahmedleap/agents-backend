package com.agentsbackend.repos;

import com.agentsbackend.entities.Transaction;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis Mapper for Transaction entity.
 * Handles all database operations for transactions including CRUD operations
 * and retrieval of recent transactions for cash movements (deposits and withdrawals).
 */
@Mapper
public interface TransactionsRepository {

    /**
     * Create a new transaction in the database.
     * Inserts transaction record with all required fields.
     *
     * @param transaction Transaction entity with populated fields
     */
    @Insert("INSERT INTO transactions (transaction_id, account_id, txn_type, amount, created_at) " +
            "VALUES (#{transactionId,jdbcType=VARCHAR}, #{account.accountId,jdbcType=VARCHAR}, " +
            "CAST(#{txnType} AS transaction_type), #{amount,jdbcType=NUMERIC}, #{createdAt})")
    void createTransaction(Transaction transaction);

    /**
     * Find transaction by ID.
     *
     * @param transactionId UUID of the transaction
     * @return Optional containing Transaction if found, empty otherwise
     */
    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at " +
            "FROM transactions WHERE transaction_id = #{transactionId,jdbcType=VARCHAR}")
    @Results({
            @Result(column = "transaction_id", property = "transactionId"),
            @Result(column = "account_id", property = "account.accountId"),
            @Result(column = "txn_type", property = "txnType"),
            @Result(column = "amount", property = "amount"),
            @Result(column = "created_at", property = "createdAt")
    })
    Optional<Transaction> findById(@Param("transactionId") UUID transactionId);

    /**
     * Find all transactions for a specific account, ordered by most recent first.
     *
     * @param accountId UUID of the account
     * @return List of Transaction entities for the account
     */
    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at " +
            "FROM transactions WHERE account_id = #{accountId,jdbcType=VARCHAR} " +
            "ORDER BY created_at DESC")
    @Results({
            @Result(column = "transaction_id", property = "transactionId"),
            @Result(column = "account_id", property = "account.accountId"),
            @Result(column = "txn_type", property = "txnType"),
            @Result(column = "amount", property = "amount"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    /**
     * Find recent transactions for a specific account with a limit.
     * Returns the N most recent transactions ordered by creation date (newest first).
     *
     * @param accountId UUID of the account
     * @param limit Maximum number of transactions to return
     * @return List of recent Transaction entities
     */
    @Select("SELECT transaction_id, account_id, txn_type, amount, created_at " +
            "FROM transactions WHERE account_id = #{accountId,jdbcType=VARCHAR} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    @Results({
            @Result(column = "transaction_id", property = "transactionId"),
            @Result(column = "account_id", property = "account.accountId"),
            @Result(column = "txn_type", property = "txnType"),
            @Result(column = "amount", property = "amount"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Transaction> findRecentTransactions(@Param("accountId") UUID accountId, @Param("limit") int limit);

    /**
     * Record a transaction for a cash movement (deposit or withdrawal).
     * Auto-generates transaction ID and uses current timestamp.
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
}
