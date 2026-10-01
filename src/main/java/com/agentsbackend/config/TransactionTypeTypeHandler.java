package com.agentsbackend.config;

import com.agentsbackend.enums.TransactionType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting TransactionType enum to/from PostgreSQL transaction_type ENUM type
 */
public class TransactionTypeTypeHandler extends BaseTypeHandler<TransactionType> {

    // Binds a TransactionType enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TransactionType parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves a TransactionType enum value from a ResultSet column by name
    @Override
    public TransactionType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return TransactionType.valueOf(value);
    }

    // Retrieves a TransactionType enum value from a ResultSet column by index
    @Override
    public TransactionType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return TransactionType.valueOf(value);
    }

    // Retrieves a TransactionType enum value from a CallableStatement column by index
    @Override
    public TransactionType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return TransactionType.valueOf(value);
    }
}
