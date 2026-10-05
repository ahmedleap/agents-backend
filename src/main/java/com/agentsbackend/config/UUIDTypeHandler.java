package com.agentsbackend.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * MyBatis type handler for converting UUID to/from database UUID type
 */
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    // Binds a UUID value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter);
    }

    // Retrieves a UUID value from a ResultSet column by name
    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    // Retrieves a UUID value from a ResultSet column by index
    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    // Retrieves a UUID value from a CallableStatement column by index
    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }
}
