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

    // Binds an AdminRole enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AdminRole parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves an AdminRole enum value from a ResultSet column by name
    @Override
    public AdminRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return AdminRole.valueOf(value);
    }

    // Retrieves an AdminRole enum value from a ResultSet column by index
    @Override
    public AdminRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AdminRole.valueOf(value);
    }

    // Retrieves an AdminRole enum value from a CallableStatement column by index
    @Override
    public AdminRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AdminRole.valueOf(value);
    }
}
