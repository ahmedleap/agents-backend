package com.agentsbackend.config;

import com.agentsbackend.enums.PortfolioSizeRange;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting PortfolioSizeRange enum to/from PostgreSQL portfolio_size_range ENUM type
 */
public class PortfolioSizeRangeTypeHandler extends BaseTypeHandler<PortfolioSizeRange> {

    // Binds a PortfolioSizeRange enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PortfolioSizeRange parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves a PortfolioSizeRange enum value from a ResultSet column by name
    @Override
    public PortfolioSizeRange getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return PortfolioSizeRange.valueOf(value);
    }

    // Retrieves a PortfolioSizeRange enum value from a ResultSet column by index
    @Override
    public PortfolioSizeRange getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return PortfolioSizeRange.valueOf(value);
    }

    // Retrieves a PortfolioSizeRange enum value from a CallableStatement column by index
    @Override
    public PortfolioSizeRange getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return PortfolioSizeRange.valueOf(value);
    }
}
