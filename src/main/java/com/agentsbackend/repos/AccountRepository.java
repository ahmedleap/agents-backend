package com.agentsbackend.repos;

import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.AccountStatus;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis Mapper for Account entity.
 * Handles all database operations for accounts including CRUD operations,
 * cash balance updates, and transaction recording.
 */
@Mapper
public interface AccountRepository {

    /**
     * Create a new account in the database.
     * Inserts account record with all required fields.
     *
     * @param account Account entity with populated fields
     */
    @Insert("INSERT INTO accounts (account_id, client_id, name, cash_balance, status, open_date) " +
            "VALUES (#{accountId,jdbcType=VARCHAR}, #{client.clientId,jdbcType=VARCHAR}, " +
            "#{name}, #{cashBalance,jdbcType=NUMERIC}, CAST(#{status} AS account_status), #{openDate})")
    void createAccount(Account account);

    /**
     * Find account by ID.
     *
     * @param accountId UUID of the account
     * @return Optional containing Account if found, empty otherwise
     */
    @Select("SELECT account_id, client_id, name, cash_balance, status, open_date " +
            "FROM accounts WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    @Results({
            @Result(column = "account_id", property = "accountId"),
            @Result(column = "client_id", property = "client.clientId"),
            @Result(column = "name", property = "name"),
            @Result(column = "cash_balance", property = "cashBalance"),
            @Result(column = "status", property = "status"),
            @Result(column = "open_date", property = "openDate")
    })
    Optional<Account> findById(@Param("accountId") UUID accountId);

    /**
     * Find all accounts for a specific client.
     *
     * @param clientId UUID of the client
     * @return List of Account objects for the client
     */
    @Select("SELECT account_id, client_id, name, cash_balance, status, open_date " +
            "FROM accounts WHERE client_id = #{clientId,jdbcType=VARCHAR} ORDER BY open_date DESC")
    @Results({
            @Result(column = "account_id", property = "accountId"),
            @Result(column = "client_id", property = "client.clientId"),
            @Result(column = "name", property = "name"),
            @Result(column = "cash_balance", property = "cashBalance"),
            @Result(column = "status", property = "status"),
            @Result(column = "open_date", property = "openDate")
    })
    List<Account> findByClientId(@Param("clientId") UUID clientId);

    /**
     * Update account details (name and/or status).
     *
     * @param account Account entity with updated fields
     */
    @Update("UPDATE accounts SET name = #{name}, status = CAST(#{status} AS account_status) " +
            "WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    void updateAccount(Account account);

    /**
     * Update the cash balance for an account.
     * Used for deposits and withdrawals.
     *
     * @param accountId UUID of the account
     * @param newBalance New cash balance value
     */
    @Update("UPDATE accounts SET cash_balance = #{newBalance,jdbcType=NUMERIC} " +
            "WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    void updateCashBalance(@Param("accountId") UUID accountId, @Param("newBalance") BigDecimal newBalance);

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
     * Check if a client exists in the database.
     *
     * @param clientId UUID of the client
     * @return true if client exists, false otherwise
     */
    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM clients WHERE client_id = #{clientId,jdbcType=VARCHAR}")
    boolean clientExists(@Param("clientId") UUID clientId);

    /**
     * Check if an account exists in the database.
     *
     * @param accountId UUID of the account
     * @return true if account exists, false otherwise
     */
    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM accounts WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    boolean existsById(@Param("accountId") UUID accountId);

    /**
     * Get count of holdings for an account.
     * Holdings represent current positions in instruments.
     *
     * @param accountId UUID of the account
     * @return Count of holdings, or null if none
     */
    @Select("SELECT COUNT(*) FROM holdings WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    Integer getHoldingCount(@Param("accountId") UUID accountId);

    /**
     * Get count of orders for an account.
     * Orders represent pending/historical trading activity.
     *
     * @param accountId UUID of the account
     * @return Count of orders, or null if none
     */
    @Select("SELECT COUNT(*) FROM orders WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    Integer getOrderCount(@Param("accountId") UUID accountId);

    /**
     * Get count of transactions for an account.
     * Transactions represent cash deposits and withdrawals.
     *
     * @param accountId UUID of the account
     * @return Count of transactions, or null if none
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    Integer getTransactionCount(@Param("accountId") UUID accountId);

    /**
     * Calculate reserved funds for open BUY orders.
     * Reserved funds = SUM(quantity * limit_price) for all PENDING BUY orders
     * This ensures funds for pending orders cannot be withdrawn.
     *
     * @param accountId UUID of the account
     * @return Sum of reserved funds, or null if no open orders
     */
    @Select("SELECT COALESCE(SUM(quantity * limit_price), 0) FROM orders " +
            "WHERE account_id = #{accountId,jdbcType=VARCHAR} " +
            "AND order_type = 'BUY' AND status = 'PENDING'")
    BigDecimal getReservedFundsForOpenOrders(@Param("accountId") UUID accountId);

    /**
     * Get total portfolio value (sum of all holdings * current price).
     * Used for portfolio summary and performance calculations.
     *
     * @param accountId UUID of the account
     * @return Total value of all holdings, or null if no holdings
     */
    @Select("SELECT COALESCE(SUM(h.quantity * COALESCE(ip.price, 0)), 0) FROM holdings h " +
            "LEFT JOIN instrument_prices ip ON h.instrument_id = ip.instrument_id " +
            "WHERE h.account_id = #{accountId,jdbcType=VARCHAR} " +
            "AND ip.as_of = (SELECT MAX(as_of) FROM instrument_prices WHERE instrument_id = h.instrument_id)")
    BigDecimal getPortfolioValue(@Param("accountId") UUID accountId);

    /**
     * Get portfolio value at a specific date (from historical snapshot).
     * Used for performance calculations.
     *
     * @param accountId UUID of the account
     * @param date Date to get portfolio value for
     * @return Portfolio value at the specified date, or null if no data
     */
    @Select("SELECT total_value FROM historical_snapshot " +
            "WHERE account_id = #{accountId,jdbcType=VARCHAR} " +
            "AND snapshot_date <= #{date,jdbcType=DATE} " +
            "ORDER BY snapshot_date DESC LIMIT 1")
    BigDecimal getPortfolioValueAtDate(@Param("accountId") UUID accountId, @Param("date") java.time.LocalDateTime date);

    /**
     * Get realized gain/loss for trades executed during a period.
     * Realized gain/loss = (sell proceeds) - (cost basis of sold holdings)
     *
     * @param accountId UUID of the account
     * @param startDate Start date for the period
     * @return Realized gains/losses, or null if no completed trades
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN o.order_type = 'SELL' " +
            "THEN (o.quantity * o.limit_price) - (h.quantity * h.average_cost_basis) ELSE 0 END), 0) " +
            "FROM orders o LEFT JOIN holdings h ON o.instrument_id = h.instrument_id " +
            "WHERE o.account_id = #{accountId,jdbcType=VARCHAR} " +
            "AND o.status = 'FILLED' AND o.created_at >= #{startDate,jdbcType=TIMESTAMP}")
    BigDecimal getRealizedGainLoss(@Param("accountId") UUID accountId, @Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Get cost basis of current holdings (total amount paid for all holdings).
     * Used to calculate unrealized gain/loss.
     *
     * @param accountId UUID of the account
     * @return Total cost basis of holdings, or null if no holdings
     */
    @Select("SELECT COALESCE(SUM(quantity * average_cost_basis), 0) FROM holdings " +
            "WHERE account_id = #{accountId,jdbcType=VARCHAR}")
    BigDecimal getHoldingsCostBasis(@Param("accountId") UUID accountId);

    /**
     * Find all accounts in the system.
     * TODO: Scope this to authenticated client in production.
     *
     * @return List of all Account objects
     */
    @Select("SELECT account_id, client_id, name, cash_balance, status, open_date FROM accounts ORDER BY open_date DESC")
    @Results({
            @Result(column = "account_id", property = "accountId"),
            @Result(column = "client_id", property = "client.clientId"),
            @Result(column = "name", property = "name"),
            @Result(column = "cash_balance", property = "cashBalance"),
            @Result(column = "status", property = "status"),
            @Result(column = "open_date", property = "openDate")
    })
    List<Account> findAll();
}
