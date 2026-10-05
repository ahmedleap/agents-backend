package com.agentsbackend.config;

import com.agentsbackend.enums.RiskTolerance;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting RiskTolerance enum to/from PostgreSQL risk_tolerance ENUM type
 */
public class RiskToleranceTypeHandler extends BaseTypeHandler<RiskTolerance> {

    // Binds a RiskTolerance enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RiskTolerance parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves a RiskTolerance enum value from a ResultSet column by name
    @Override
    public RiskTolerance getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return RiskTolerance.valueOf(value);
    }

    // Retrieves a RiskTolerance enum value from a ResultSet column by index
    @Override
    public RiskTolerance getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return RiskTolerance.valueOf(value);
    }

    // Retrieves a RiskTolerance enum value from a CallableStatement column by index
    @Override
    public RiskTolerance getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return RiskTolerance.valueOf(value);
    }
}
