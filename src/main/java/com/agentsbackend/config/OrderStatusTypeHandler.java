package com.agentsbackend.config;

import com.agentsbackend.enums.OrderStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting OrderStatus enum to/from PostgreSQL order_status ENUM type
 */
public class OrderStatusTypeHandler extends BaseTypeHandler<OrderStatus> {

    // Binds an OrderStatus enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OrderStatus parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves an OrderStatus enum value from a ResultSet column by name
    @Override
    public OrderStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return OrderStatus.valueOf(value);
    }

    // Retrieves an OrderStatus enum value from a ResultSet column by index
    @Override
    public OrderStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return OrderStatus.valueOf(value);
    }

    // Retrieves an OrderStatus enum value from a CallableStatement column by index
    @Override
    public OrderStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return OrderStatus.valueOf(value);
    }
}
