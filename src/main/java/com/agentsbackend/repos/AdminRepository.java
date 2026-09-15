package com.agentsbackend.repos;

import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AdminRepository {

    @Insert("INSERT INTO admin (admin_id, first_name, last_name, email, password_hash, role, created_at) " +
            "VALUES (#{adminId}, #{firstName}, #{lastName}, #{email}, #{passwordHash}, #{role}, #{createdAt})")
    void createAdmin(Admin admin);

    @Select("SELECT * FROM admin WHERE email = #{email}")
    Optional<Admin> findByEmail(String email);

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    Optional<Admin> findById(UUID adminId);

    @Select("SELECT * FROM admin")
    List<Admin> findAll();

    @Select("SELECT * FROM admin WHERE role = #{role}")
    List<Admin> findByRole(AdminRole role);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM admin WHERE admin_id = #{adminId}")
    boolean existsById(UUID adminId);
}

