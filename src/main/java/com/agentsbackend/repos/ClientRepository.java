package com.agentsbackend.repos;

import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ClientRepository {

    @Insert("INSERT INTO clients (client_id, first_name, middle_name, last_name, email, password_hash, " +
            "date_of_birth, join_date, ssn_last4, portfolio_size_range, risk_tolerance, is_active) " +
            "VALUES (#{clientId}, #{firstName}, #{middleName}, #{lastName}, #{email}, #{passwordHash}, " +
            "#{dateOfBirth}, #{joinDate}, #{ssnLast4}, " +
            "CAST(#{portfolioSizeRange} AS portfolio_size_range), " +
            "CAST(#{riskTolerance} AS risk_tolerance), true)")
    void createClient(Client client);

    @Select("SELECT * FROM clients WHERE email = #{email} AND is_active = true")
    Optional<Client> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM clients WHERE is_active = true")
    List<Client> findAll();

    @Select("SELECT * FROM clients WHERE last_name = #{lastName} AND is_active = true")
    List<Client> findByLastName(@Param("lastName") String lastName);

    @Select("SELECT * FROM clients WHERE risk_tolerance = CAST(#{riskTolerance} AS risk_tolerance) AND is_active = true")
    List<Client> findByRiskTolerance(@Param("riskTolerance") RiskTolerance riskTolerance);

    @Select("SELECT * FROM clients WHERE portfolio_size_range = CAST(#{portfolioSizeRange} AS portfolio_size_range) AND is_active = true")
    List<Client> findByPortfolioSizeRange(@Param("portfolioSizeRange") PortfolioSizeRange portfolioSizeRange);

    @Select("SELECT * FROM clients WHERE join_date >= #{since} AND is_active = true ORDER BY join_date DESC")
    List<Client> findClientsJoinedSince(@Param("since") LocalDateTime since);

    @Select("SELECT * FROM clients WHERE join_date >= #{start} AND join_date < #{end} AND is_active = true ORDER BY join_date DESC")
    List<Client> findClientsJoinedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT * FROM clients WHERE CAST(join_date AS DATE) = #{date} AND is_active = true ORDER BY join_date DESC")
    List<Client> findClientsJoinedOn(@Param("date") LocalDate date);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM clients WHERE email = #{email} AND is_active = true")
    boolean existsByEmail(@Param("email") String email);

    @Update("UPDATE clients SET portfolio_size_range = CAST(#{portfolioSizeRange} AS portfolio_size_range), " +
            "risk_tolerance = CAST(#{riskTolerance} AS risk_tolerance) " +
            "WHERE client_id = #{clientId,jdbcType=VARCHAR} AND is_active = true")
    void updateClient(Client client);

    @Update("UPDATE clients SET is_active = false WHERE client_id = #{clientId,jdbcType=VARCHAR}")
    void deleteClient(@Param("clientId") UUID clientId);
}
