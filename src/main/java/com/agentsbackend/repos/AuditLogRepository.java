package com.agentsbackend.repos;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.AuditLog;
import java.util.UUID;
import java.util.List;

@Mapper
public interface AuditLogRepository {
    
    @Insert("INSERT INTO audit_logs (audit_log_id, order_id, account_id, client_id, event_type, event_time, reason, details, created_at) " +
            "VALUES (#{auditLogId}, #{orderId}, #{accountId}, #{clientId}, #{eventType}, #{eventTime}, #{reason}, #{details}::jsonb, #{createdAt})")
    void save(AuditLog auditLog);

    @Select("SELECT * FROM audit_logs WHERE order_id = #{orderId}")
    List<AuditLog> findByOrderId(@Param("orderId") UUID orderId);

    @Select("SELECT * FROM audit_logs WHERE account_id = #{accountId} ORDER BY event_time DESC")
    List<AuditLog> findByAccountId(@Param("accountId") UUID accountId);

    @Select("SELECT * FROM audit_logs WHERE client_id = #{clientId} ORDER BY event_time DESC")
    List<AuditLog> findByClientId(@Param("clientId") UUID clientId);

    @Select("SELECT * FROM audit_logs WHERE event_type = #{eventType} ORDER BY event_time DESC")
    List<AuditLog> findByEventType(@Param("eventType") String eventType);
}
