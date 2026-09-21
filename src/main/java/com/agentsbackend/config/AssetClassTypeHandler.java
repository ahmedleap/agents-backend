package com.agentsbackend.config;

import com.agentsbackend.enums.AssetClass;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting AssetClass enum to/from PostgreSQL asset_class ENUM type
 */
public class AssetClassTypeHandler extends BaseTypeHandler<AssetClass> {

    // Binds an AssetClass enum value to a PreparedStatement parameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AssetClass parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.VARCHAR);
    }

    // Retrieves an AssetClass enum value from a ResultSet column by name
    @Override
    public AssetClass getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return AssetClass.valueOf(value);
    }

    // Retrieves an AssetClass enum value from a ResultSet column by index
    @Override
    public AssetClass getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AssetClass.valueOf(value);
    }

    // Retrieves an AssetClass enum value from a CallableStatement column by index
    @Override
    public AssetClass getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return AssetClass.valueOf(value);
    }
}
