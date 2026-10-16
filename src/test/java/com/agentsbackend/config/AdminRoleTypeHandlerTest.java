package com.agentsbackend.config;

import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminRoleTypeHandlerTest {

    private final AdminRoleTypeHandler handler = new AdminRoleTypeHandler();

    @Test
    void bindsRoleAsVarchar() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);

        handler.setNonNullParameter(statement, 2, AdminRole.ADMIN, JdbcType.VARCHAR);

        verify(statement).setObject(2, "ADMIN", java.sql.Types.VARCHAR);
    }

    @Test
    void readsRoleByColumnNameAndReturnsNull() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("role")).thenReturn("ANALYST");
        assertEquals(AdminRole.ANALYST, handler.getNullableResult(resultSet, "role"));

        when(resultSet.getString("empty_role")).thenReturn(null);
        assertNull(handler.getNullableResult(resultSet, "empty_role"));
    }

    @Test
    void readsRoleByColumnIndexAndCallableStatement() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("ADMIN");
        assertEquals(AdminRole.ADMIN, handler.getNullableResult(resultSet, 1));
        when(resultSet.getString(3)).thenReturn(null);
        assertNull(handler.getNullableResult(resultSet, 3));

        CallableStatement statement = mock(CallableStatement.class);
        when(statement.getString(2)).thenReturn("ANALYST");
        assertEquals(AdminRole.ANALYST, handler.getNullableResult(statement, 2));

        when(statement.getString(3)).thenReturn(null);
        assertNull(handler.getNullableResult(statement, 3));
    }
}