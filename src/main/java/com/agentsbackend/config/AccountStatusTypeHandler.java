package com.agentsbackend.config;

import com.agentsbackend.enums.AccountStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting AccountStatus enum to/from PostgreSQL account_status ENUM type
 */
public class AccountStatusTypeHandler extends BaseTypeHandler<AccountStatus> {

    // Binds an AccountStatus enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AccountStatus parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves an AccountStatus enum value from a ResultSet column by name
    @Override
    public AccountStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return AccountStatus.valueOf(value);
    }

    // Retrieves an AccountStatus enum value from a ResultSet column by index
    @Override
    public AccountStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AccountStatus.valueOf(value);
    }

    // Retrieves an AccountStatus enum value from a CallableStatement column by index
    @Override
    public AccountStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AccountStatus.valueOf(value);
    }
}
