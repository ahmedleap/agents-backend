package com.agentsbackend.repos.transactional;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.DTO.response.AdminAccountResponse;
import com.agentsbackend.DTO.response.AdminAuditResponse;
import com.agentsbackend.DTO.response.AdminClientResponse;
import com.agentsbackend.DTO.response.AdminOrderResponse;
import com.agentsbackend.DTO.response.AdminInsightsResponse;
import com.agentsbackend.DTO.response.TradingActivityReportResponse;
import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository {

    @Select("SELECT client_id AS \"clientId\", CONCAT_WS(' ', first_name, middle_name, last_name) AS \"clientName\", " +
        "first_name AS \"firstName\", middle_name AS \"middleName\", " +
        "last_name AS \"lastName\", email, date_of_birth AS \"dateOfBirth\", join_date AS \"joinDate\", " +
        "ssn_last4 AS \"ssnLast4\", portfolio_size_range AS \"portfolioSizeRange\", " +
        "risk_tolerance AS \"riskTolerance\" FROM clients ORDER BY join_date DESC")
    List<AdminClientResponse> findAllClients();

    @Select("SELECT client_id AS \"clientId\", CONCAT_WS(' ', first_name, middle_name, last_name) AS \"clientName\", " +
        "first_name AS \"firstName\", middle_name AS \"middleName\", " +
        "last_name AS \"lastName\", email, date_of_birth AS \"dateOfBirth\", join_date AS \"joinDate\", " +
        "ssn_last4 AS \"ssnLast4\", portfolio_size_range AS \"portfolioSizeRange\", " +
        "risk_tolerance AS \"riskTolerance\" FROM clients WHERE client_id = #{clientId}")
    Optional<AdminClientResponse> findClientById(@Param("clientId") UUID clientId);

    @Select("SELECT a.account_id AS \"accountId\", a.client_id AS \"clientId\", " +
        "CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", a.name, " +
        "a.cash_balance AS \"cashBalance\", a.status, a.open_date AS \"openDate\" " +
        "FROM accounts a JOIN clients c ON c.client_id = a.client_id ORDER BY a.open_date DESC")
    List<AdminAccountResponse> findAllAccounts();

    @Select("SELECT a.account_id AS \"accountId\", a.client_id AS \"clientId\", " +
        "CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", a.name, " +
        "a.cash_balance AS \"cashBalance\", a.status, a.open_date AS \"openDate\" " +
        "FROM accounts a JOIN clients c ON c.client_id = a.client_id WHERE a.account_id = #{accountId}")
    Optional<AdminAccountResponse> findAccountById(@Param("accountId") UUID accountId);

    @Select("SELECT o.order_id AS \"orderId\", o.account_id AS \"accountId\", " +
        "a.client_id AS \"clientId\", CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", " +
        "o.instrument_id AS \"instrumentId\", i.ticker, " +
        "i.name AS \"instrumentName\", o.order_type AS \"orderType\", o.quantity, " +
        "o.limit_price AS \"limitPrice\", o.filled_price AS \"filledPrice\", o.status, " +
        "o.created_at AS \"createdAt\", o.filled_at AS \"filledAt\", " +
        "o.cancelled_at AS \"cancelledAt\", o.cancel_reason AS \"cancelReason\" FROM orders o " +
        "JOIN accounts a ON a.account_id = o.account_id " +
        "JOIN clients c ON c.client_id = a.client_id " +
        "JOIN instruments i ON i.instrument_id = o.instrument_id " +
        "ORDER BY o.created_at DESC")
    List<AdminOrderResponse> findAllOrders();

    @Select("SELECT o.order_id AS \"orderId\", o.account_id AS \"accountId\", " +
        "a.client_id AS \"clientId\", CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", " +
        "o.instrument_id AS \"instrumentId\", i.ticker, " +
        "i.name AS \"instrumentName\", o.order_type AS \"orderType\", o.quantity, " +
        "o.limit_price AS \"limitPrice\", o.filled_price AS \"filledPrice\", o.status, " +
        "o.created_at AS \"createdAt\", o.filled_at AS \"filledAt\", " +
        "o.cancelled_at AS \"cancelledAt\", o.cancel_reason AS \"cancelReason\" FROM orders o " +
        "JOIN accounts a ON a.account_id = o.account_id " +
        "JOIN clients c ON c.client_id = a.client_id " +
        "JOIN instruments i ON i.instrument_id = o.instrument_id " +
        "WHERE o.order_id = #{orderId}")
    Optional<AdminOrderResponse> findOrderById(@Param("orderId") UUID orderId);

    @Select("SELECT o.order_id AS \"orderId\", o.account_id AS \"accountId\", " +
        "a.client_id AS \"clientId\", CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", " +
        "o.instrument_id AS \"instrumentId\", i.ticker, " +
        "i.name AS \"instrumentName\", o.order_type AS \"orderType\", o.quantity, " +
        "o.limit_price AS \"limitPrice\", o.filled_price AS \"filledPrice\", o.status, " +
        "o.created_at AS \"createdAt\", o.filled_at AS \"filledAt\", " +
        "o.cancelled_at AS \"cancelledAt\", o.cancel_reason AS \"cancelReason\" FROM orders o " +
        "JOIN accounts a ON a.account_id = o.account_id " +
        "JOIN clients c ON c.client_id = a.client_id " +
        "JOIN instruments i ON i.instrument_id = o.instrument_id " +
        "WHERE o.order_id = #{tradeId} AND o.status = 'FILLED'")
    Optional<AdminOrderResponse> findTradeById(@Param("tradeId") UUID tradeId);

    @Select("SELECT al.audit_log_id AS \"auditLogId\", al.order_id AS \"orderId\", " +
        "al.account_id AS \"accountId\", al.client_id AS \"clientId\", " +
        "CONCAT_WS(' ', c.first_name, c.middle_name, c.last_name) AS \"clientName\", " +
        "al.event_type AS \"eventType\", al.event_time AS \"eventTime\", al.reason, " +
        "al.details::text AS details, al.created_at AS \"createdAt\" " +
        "FROM audit_logs al JOIN clients c ON c.client_id = al.client_id " +
        "WHERE al.order_id = #{orderId} ORDER BY al.event_time DESC")
    List<AdminAuditResponse> findAuditByOrderId(@Param("orderId") UUID orderId);

    @Select("SELECT COUNT(*) AS \"totalOrders\", " +
        "COUNT(*) FILTER (WHERE status = 'FILLED') AS \"filledOrders\", " +
        "COUNT(*) FILTER (WHERE status = 'PENDING') AS \"pendingOrders\", " +
        "COUNT(*) FILTER (WHERE status = 'CANCELLED') AS \"cancelledOrders\", " +
        "COALESCE(SUM(quantity) FILTER (WHERE status = 'FILLED'), 0) AS \"filledQuantity\", " +
        "COALESCE(SUM(quantity * COALESCE(filled_price, limit_price)) FILTER (WHERE status = 'FILLED'), 0) AS \"tradedValue\" " +
        "FROM orders")
    TradingActivityReportResponse getTradingActivityReport();

    @Select("SELECT (SELECT COUNT(*) FROM clients) AS \"clientCount\", " +
        "(SELECT COUNT(*) FROM accounts) AS \"accountCount\", " +
        "(SELECT COUNT(*) FROM orders) AS \"orderCount\", " +
        "(SELECT COUNT(*) FROM orders WHERE status = 'FILLED') AS \"filledOrderCount\", " +
        "(SELECT COALESCE(SUM(quantity * COALESCE(filled_price, limit_price)), 0) " +
        "FROM orders WHERE status = 'FILLED') AS \"tradedValue\"")
    AdminInsightsResponse getInsights();

    @Insert("INSERT INTO admin (admin_id, first_name, last_name, email, password_hash, role, created_at) " +
            "VALUES (#{adminId,jdbcType=OTHER}, #{firstName}, #{lastName}, #{email}, #{passwordHash}, CAST(#{role} AS admin_role), #{createdAt})")
    void createAdmin(Admin admin);

    @Select("SELECT * FROM admin WHERE email = #{email}")
    Optional<Admin> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId,jdbcType=OTHER}")
    Optional<Admin> findById(@Param("adminId") UUID adminId);

    @Select("SELECT * FROM admin")
    List<Admin> findAll();

    @Select("SELECT * FROM admin WHERE role = #{role}")
    List<Admin> findByRole(@Param("role") AdminRole role);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM admin WHERE admin_id = #{adminId,jdbcType=OTHER}")
    boolean existsById(@Param("adminId") UUID adminId);
}

