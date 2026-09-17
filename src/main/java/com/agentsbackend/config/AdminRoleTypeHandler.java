package com.agentsbackend.config;

import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting AdminRole enum to/from PostgreSQL admin_role ENUM type
 */
public class AdminRoleTypeHandler extends BaseTypeHandler<AdminRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AdminRole parameter, JdbcType jdbcType) 
            throws SQLException {
        // Cast to TEXT then to admin_role for PostgreSQL ENUM
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    @Override
    public AdminRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : AdminRole.valueOf(value);
    }

    @Override
    public AdminRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : AdminRole.valueOf(value);
    }

    @Override
    public AdminRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : AdminRole.valueOf(value);
    }
}
