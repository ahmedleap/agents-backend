package com.agentsbackend.repos.transactional;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the SQL that MyBatis builds for ClientRepository.updateClient, without a database:
 * only the fields that were sent should appear in the SET clause.
 */
@DisplayName("ClientRepository updateClient SQL")
class ClientRepositorySqlTest {

    private MappedStatement updateClient;

    @BeforeEach
    void setUp() {
        Configuration configuration = new Configuration();
        configuration.addMapper(ClientRepository.class);
        updateClient = configuration.getMappedStatement(ClientRepository.class.getName() + ".updateClient");
    }

    /** The SQL MyBatis would run for this request, with whitespace collapsed. */
    private String sqlFor(UpdateClientRequestDTO request) {
        MapperMethod.ParamMap<Object> params = new MapperMethod.ParamMap<>();
        params.put("clientId", UUID.randomUUID());
        params.put("req", request);
        params.put("param1", params.get("clientId"));
        params.put("param2", request);
        return updateClient.getBoundSql(params).getSql().replaceAll("\\s+", " ").trim();
    }

    @Test
    @DisplayName("One field: only that column is in SET")
    void oneField() {
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setFirstName("Jane");

        assertEquals("UPDATE clients SET first_name = ? WHERE client_id = ?", sqlFor(request));
    }

    @Test
    @DisplayName("Empty middle name is still updated, through NULLIF, so it clears the column")
    void emptyMiddleNameIsUpdated() {
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setMiddleName("");

        assertEquals("UPDATE clients SET middle_name = NULLIF(?, '') WHERE client_id = ?", sqlFor(request));
    }

    @Test
    @DisplayName("Several fields: each sent column is in SET, with no trailing comma")
    void severalFields() {
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setLastName("Doe");
        request.setMiddleName("Q");

        assertEquals(
                "UPDATE clients SET middle_name = NULLIF(?, ''), last_name = ? WHERE client_id = ?",
                sqlFor(request));
    }

    @Test
    @DisplayName("All fields: middle name uses NULLIF, enums are cast, phone and country are never updated")
    void allFields() {
        UpdateClientRequestDTO request = new UpdateClientRequestDTO(
                "Jane", "Q", "Doe", RiskTolerance.MODERATE, PortfolioSizeRange.UNDER_50K);

        assertEquals(
                "UPDATE clients SET first_name = ?, middle_name = NULLIF(?, ''), last_name = ?, "
                        + "risk_tolerance = CAST(? AS risk_tolerance), "
                        + "portfolio_size_range = CAST(? AS portfolio_size_range) WHERE client_id = ?",
                sqlFor(request));
    }

    @Test
    @DisplayName("Fields left null are not in SET")
    void nullFieldsLeftOut() {
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setRiskTolerance(RiskTolerance.AGGRESSIVE);

        String sql = sqlFor(request);

        assertFalse(sql.contains("first_name"));
        assertFalse(sql.contains("last_name"));
        assertEquals("UPDATE clients SET risk_tolerance = CAST(? AS risk_tolerance) WHERE client_id = ?", sql);
    }
}
