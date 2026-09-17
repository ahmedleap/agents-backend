package com.agentsbackend.repos;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AdminRepository {

    @Insert("INSERT INTO admin (admin_id, first_name, last_name, email, password_hash, role, created_at) " +
            "VALUES (#{adminId,jdbcType=VARCHAR}, #{firstName}, #{lastName}, #{email}, #{passwordHash}, CAST(#{role} AS admin_role), #{createdAt})")
    void createAdmin(Admin admin);

    @Select("SELECT * FROM admin WHERE email = #{email}")
    Optional<Admin> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId,jdbcType=VARCHAR}")
    Optional<Admin> findById(@Param("adminId") UUID adminId);

    @Select("SELECT * FROM admin")
    List<Admin> findAll();

    @Select("SELECT * FROM admin WHERE role = #{role}")
    List<Admin> findByRole(@Param("role") AdminRole role);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM admin WHERE admin_id = #{adminId,jdbcType=VARCHAR}")
    boolean existsById(@Param("adminId") UUID adminId);
}

