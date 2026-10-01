package com.agentsbackend.config;

import com.agentsbackend.enums.OrderType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting OrderType enum to/from PostgreSQL order_type ENUM type
 */
public class OrderTypeTypeHandler extends BaseTypeHandler<OrderType> {

    // Binds an OrderType enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OrderType parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves an OrderType enum value from a ResultSet column by name
    @Override
    public OrderType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return OrderType.valueOf(value);
    }

    // Retrieves an OrderType enum value from a ResultSet column by index
    @Override
    public OrderType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return OrderType.valueOf(value);
    }

    // Retrieves an OrderType enum value from a CallableStatement column by index
    @Override
    public OrderType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return OrderType.valueOf(value);
    }
}
