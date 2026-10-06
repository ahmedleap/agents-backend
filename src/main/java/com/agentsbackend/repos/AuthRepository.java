package com.agentsbackend.repos;

import com.agentsbackend.entities.AuthSession;
import com.agentsbackend.entities.AuthUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AuthRepository {
    @Select("SELECT client_id AS clientId, first_name AS firstName, middle_name AS middleName, " +
            "last_name AS lastName, email, password_hash AS passwordHash, phone, country, " +
            "email_verified AS emailVerified, auth_status AS authStatus, " +
            "failed_login_attempts AS failedLoginAttempts, locked_until AS lockedUntil, " +
            "date_of_birth AS dateOfBirth, join_date AS joinDate FROM clients WHERE email = #{email}")
    Optional<AuthUser> findByEmail(@Param("email") String email);

    @Select("SELECT client_id AS clientId, first_name AS firstName, middle_name AS middleName, " +
            "last_name AS lastName, email, password_hash AS passwordHash, phone, country, " +
            "email_verified AS emailVerified, auth_status AS authStatus, " +
            "failed_login_attempts AS failedLoginAttempts, locked_until AS lockedUntil, " +
            "date_of_birth AS dateOfBirth, join_date AS joinDate FROM clients WHERE client_id = #{clientId}")
    Optional<AuthUser> findById(@Param("clientId") UUID clientId);

    @Insert("INSERT INTO clients (client_id, first_name, last_name, email, password_hash, phone, country, " +
            "email_verified, auth_status, date_of_birth, join_date, signup_ip, signup_device) " +
            "VALUES (#{clientId}, #{firstName}, #{lastName}, #{email}, #{passwordHash}, #{phone}, #{country}, " +
            "TRUE, 'ACTIVE', #{dateOfBirth}, #{joinDate}, " +
            "CAST(NULLIF(#{signupIp}, '') AS INET), #{signupDevice})")
    void insertClient(@Param("clientId") UUID clientId, @Param("firstName") String firstName,
                      @Param("lastName") String lastName, @Param("email") String email,
                      @Param("passwordHash") String passwordHash, @Param("phone") String phone,
                      @Param("country") String country, @Param("dateOfBirth") LocalDate dateOfBirth,
                      @Param("joinDate") LocalDateTime joinDate, @Param("signupIp") String signupIp,
                      @Param("signupDevice") String signupDevice);

    @Insert("INSERT INTO accounts (account_id, client_id, name, cash_balance, status, open_date) " +
            "VALUES (#{accountId}, #{clientId}, 'Primary Trading Account', 0, 'ACTIVE', #{createdAt})")
    void insertInitialAccount(@Param("accountId") UUID accountId, @Param("clientId") UUID clientId,
                              @Param("createdAt") LocalDateTime createdAt);

    @Select("SELECT account_id AS accountId, cash_balance AS cashBalance, status, open_date AS openDate " +
            "FROM accounts WHERE client_id = #{clientId} ORDER BY open_date")
    List<Map<String, Object>> findAccounts(@Param("clientId") UUID clientId);

    @Update("UPDATE clients SET failed_login_attempts = failed_login_attempts + 1, " +
            "locked_until = CASE WHEN failed_login_attempts + 1 >= 4 THEN CURRENT_TIMESTAMP + INTERVAL '30 minutes' ELSE locked_until END, " +
            "auth_status = CASE WHEN failed_login_attempts + 1 >= 4 THEN 'LOCKED' ELSE auth_status END " +
            "WHERE client_id = #{clientId} AND auth_status = 'ACTIVE'")
    int recordFailedLogin(@Param("clientId") UUID clientId);

    @Update("UPDATE clients SET failed_login_attempts = 0, locked_until = NULL " +
            "WHERE client_id = #{clientId} AND auth_status = 'ACTIVE'")
    int clearFailedLogins(@Param("clientId") UUID clientId);

    @Update("UPDATE clients SET auth_status = 'ACTIVE', failed_login_attempts = 0, locked_until = NULL " +
            "WHERE client_id = #{clientId} AND auth_status = 'LOCKED' " +
            "AND locked_until IS NOT NULL AND locked_until <= CURRENT_TIMESTAMP AND email_verified = TRUE")
    int unlockIfExpired(@Param("clientId") UUID clientId);

    @Insert("INSERT INTO auth_sessions (session_id, client_id, access_token_hash, refresh_token_hash, " +
            "created_at, last_active, access_expires_at, refresh_expires_at, device, location) " +
            "VALUES (#{sessionId}, #{clientId}, #{accessTokenHash}, #{refreshTokenHash}, #{createdAt}, " +
            "#{lastActive}, #{accessExpiresAt}, #{refreshExpiresAt}, #{device}, #{location})")
    void insertSession(AuthSession session);

    @Select("SELECT session_id AS sessionId, client_id AS clientId, access_token_hash AS accessTokenHash, " +
            "refresh_token_hash AS refreshTokenHash, created_at AS createdAt, last_active AS lastActive, " +
            "access_expires_at AS accessExpiresAt, refresh_expires_at AS refreshExpiresAt, " +
            "revoked_at AS revokedAt, device, location FROM auth_sessions WHERE access_token_hash = #{hash}")
    Optional<AuthSession> findByAccessHash(@Param("hash") String hash);

    @Select("SELECT session_id AS sessionId, client_id AS clientId, access_token_hash AS accessTokenHash, " +
            "refresh_token_hash AS refreshTokenHash, created_at AS createdAt, last_active AS lastActive, " +
            "access_expires_at AS accessExpiresAt, refresh_expires_at AS refreshExpiresAt, " +
            "revoked_at AS revokedAt, device, location FROM auth_sessions WHERE refresh_token_hash = #{hash}")
    Optional<AuthSession> findByRefreshHash(@Param("hash") String hash);

    @Update("UPDATE auth_sessions SET last_active = CURRENT_TIMESTAMP WHERE session_id = #{sessionId} " +
            "AND revoked_at IS NULL AND access_expires_at > CURRENT_TIMESTAMP " +
            "AND last_active > CURRENT_TIMESTAMP - INTERVAL '30 minutes'")
    int touchSession(@Param("sessionId") UUID sessionId);

    @Update("UPDATE auth_sessions SET access_token_hash = #{newAccessHash}, refresh_token_hash = #{newRefreshHash}, " +
            "access_expires_at = #{accessExpiresAt}, refresh_expires_at = #{refreshExpiresAt}, last_active = CURRENT_TIMESTAMP " +
            "WHERE session_id = #{sessionId} AND refresh_token_hash = #{oldRefreshHash} " +
            "AND revoked_at IS NULL AND refresh_expires_at > CURRENT_TIMESTAMP " +
            "AND last_active > CURRENT_TIMESTAMP - INTERVAL '30 minutes'")
    int rotateSession(@Param("sessionId") UUID sessionId, @Param("oldRefreshHash") String oldRefreshHash,
                      @Param("newAccessHash") String newAccessHash, @Param("newRefreshHash") String newRefreshHash,
                      @Param("accessExpiresAt") LocalDateTime accessExpiresAt,
                      @Param("refreshExpiresAt") LocalDateTime refreshExpiresAt);

    @Update("UPDATE auth_sessions SET revoked_at = CURRENT_TIMESTAMP WHERE session_id = #{sessionId} " +
            "AND client_id = #{clientId} AND revoked_at IS NULL")
    int revokeSession(@Param("sessionId") UUID sessionId, @Param("clientId") UUID clientId);

    @Update("UPDATE auth_sessions SET revoked_at = CURRENT_TIMESTAMP WHERE access_token_hash = #{hash} AND revoked_at IS NULL")
    int revokeByAccessHash(@Param("hash") String hash);

    @Update("UPDATE auth_sessions SET revoked_at = CURRENT_TIMESTAMP WHERE client_id = #{clientId} AND revoked_at IS NULL")
    int revokeAllSessions(@Param("clientId") UUID clientId);

    @Select("SELECT session_id AS sessionId, client_id AS clientId, created_at AS createdAt, " +
            "last_active AS lastActive, access_expires_at AS accessExpiresAt, " +
            "refresh_expires_at AS refreshExpiresAt, revoked_at AS revokedAt, device, location " +
            "FROM auth_sessions WHERE client_id = #{clientId} AND revoked_at IS NULL " +
            "AND refresh_expires_at > CURRENT_TIMESTAMP " +
            "AND last_active > CURRENT_TIMESTAMP - INTERVAL '30 minutes' ORDER BY last_active DESC")
    List<AuthSession> findActiveSessions(@Param("clientId") UUID clientId);

}
