package com.agentsbackend.DTO.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EntirePortfolioResponseDTO Tests")
class EntirePortfolioResponseDTOTest {

    private String clientId;
    private List<GetAccountPortfolioResponseDTO> accounts;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalGainLossDollars;
    private BigDecimal totalGainLossPercent;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID().toString();
        accounts = new ArrayList<>();
        totalPortfolioValue = new BigDecimal("50000.00");
        totalCostBasis = new BigDecimal("45000.00");
        totalGainLossDollars = new BigDecimal("5000.00");
        totalGainLossPercent = new BigDecimal("11.11");
    }

    @Test
    @DisplayName("Should create DTO with no-arg constructor")
    void testNoArgConstructor() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();

        assertNotNull(dto);
        assertNull(dto.getClientId());
        assertNull(dto.getAccounts());
        assertNull(dto.getTotalPortfolioValue());
        assertNull(dto.getTotalCostBasis());
        assertNull(dto.getTotalGainLossDollars());
        assertNull(dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should create DTO with full constructor")
    void testFullConstructor() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO(
            clientId, accounts, totalPortfolioValue,
            totalCostBasis, totalGainLossDollars, totalGainLossPercent
        );

        assertEquals(clientId, dto.getClientId());
        assertEquals(accounts, dto.getAccounts());
        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
        assertEquals(totalCostBasis, dto.getTotalCostBasis());
        assertEquals(totalGainLossDollars, dto.getTotalGainLossDollars());
        assertEquals(totalGainLossPercent, dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should set and get clientId")
    void testSetAndGetClientId() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setClientId(clientId);

        assertEquals(clientId, dto.getClientId());
    }

    @Test
    @DisplayName("Should set and get accounts")
    void testSetAndGetAccounts() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        GetAccountPortfolioResponseDTO account = new GetAccountPortfolioResponseDTO();
        accounts.add(account);

        dto.setAccounts(accounts);

        assertEquals(accounts, dto.getAccounts());
        assertEquals(1, dto.getAccounts().size());
    }

    @Test
    @DisplayName("Should set and get totalPortfolioValue")
    void testSetAndGetTotalPortfolioValue() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setTotalPortfolioValue(totalPortfolioValue);

        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should set and get totalCostBasis")
    void testSetAndGetTotalCostBasis() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setTotalCostBasis(totalCostBasis);

        assertEquals(totalCostBasis, dto.getTotalCostBasis());
    }

    @Test
    @DisplayName("Should set and get totalGainLossDollars")
    void testSetAndGetTotalGainLossDollars() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setTotalGainLossDollars(totalGainLossDollars);

        assertEquals(totalGainLossDollars, dto.getTotalGainLossDollars());
    }

    @Test
    @DisplayName("Should set and get totalGainLossPercent")
    void testSetAndGetTotalGainLossPercent() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setTotalGainLossPercent(totalGainLossPercent);

        assertEquals(totalGainLossPercent, dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should handle null accounts list")
    void testSetAndGetNullAccounts() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        dto.setAccounts(null);

        assertNull(dto.getAccounts());
    }

    @Test
    @DisplayName("Should handle empty accounts list")
    void testSetAndGetEmptyAccounts() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        List<GetAccountPortfolioResponseDTO> emptyList = new ArrayList<>();
        dto.setAccounts(emptyList);

        assertNotNull(dto.getAccounts());
        assertTrue(dto.getAccounts().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple accounts")
    void testMultipleAccounts() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        GetAccountPortfolioResponseDTO account1 = new GetAccountPortfolioResponseDTO();
        GetAccountPortfolioResponseDTO account2 = new GetAccountPortfolioResponseDTO();
        accounts.add(account1);
        accounts.add(account2);

        dto.setAccounts(accounts);

        assertEquals(2, dto.getAccounts().size());
    }

    @Test
    @DisplayName("Should handle large portfolio values")
    void testLargePortfolioValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        BigDecimal largeValue = new BigDecimal("10000000.00");

        dto.setTotalPortfolioValue(largeValue);

        assertEquals(largeValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should handle zero portfolio values")
    void testZeroPortfolioValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        BigDecimal zeroValue = BigDecimal.ZERO;

        dto.setTotalPortfolioValue(zeroValue);
        dto.setTotalGainLossDollars(zeroValue);

        assertEquals(zeroValue, dto.getTotalPortfolioValue());
        assertEquals(zeroValue, dto.getTotalGainLossDollars());
    }

    @Test
    @DisplayName("Should handle negative gain loss values")
    void testNegativeGainLossValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        BigDecimal negativeValue = new BigDecimal("-2500.00");

        dto.setTotalGainLossDollars(negativeValue);
        dto.setTotalGainLossPercent(new BigDecimal("-5.55"));

        assertEquals(negativeValue, dto.getTotalGainLossDollars());
        assertEquals(new BigDecimal("-5.55"), dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should update multiple fields")
    void testUpdateMultipleFields() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();

        dto.setClientId(clientId);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setTotalCostBasis(totalCostBasis);
        dto.setTotalGainLossDollars(totalGainLossDollars);

        assertEquals(clientId, dto.getClientId());
        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
        assertEquals(totalCostBasis, dto.getTotalCostBasis());
        assertEquals(totalGainLossDollars, dto.getTotalGainLossDollars());
    }

    @Test
    @DisplayName("Should overwrite previous values")
    void testOverwriteValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        String oldClientId = "old-client-id";
        String newClientId = "new-client-id";

        dto.setClientId(oldClientId);
        assertEquals(oldClientId, dto.getClientId());

        dto.setClientId(newClientId);
        assertEquals(newClientId, dto.getClientId());
    }

    @Test
    @DisplayName("Should handle precision in BigDecimal fields")
    void testPrecisionInBigDecimalFields() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        BigDecimal preciseValue = new BigDecimal("12345.6789");

        dto.setTotalPortfolioValue(preciseValue);

        assertEquals(preciseValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should construct with all null values in full constructor")
    void testFullConstructorWithNullValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO(
            null, null, null, null, null, null
        );

        assertNull(dto.getClientId());
        assertNull(dto.getAccounts());
        assertNull(dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should preserve account objects in list")
    void testPreserveAccountObjects() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        GetAccountPortfolioResponseDTO account = new GetAccountPortfolioResponseDTO();
        account.setAccountId("ACC-001");
        accounts.add(account);

        dto.setAccounts(accounts);

        assertEquals("ACC-001", dto.getAccounts().get(0).getAccountId());
    }

    @Test
    @DisplayName("Should handle multiple accounts with different values")
    void testMultipleAccountsWithDifferentValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        
        GetAccountPortfolioResponseDTO account1 = new GetAccountPortfolioResponseDTO();
        account1.setAccountId("ACC-001");
        account1.setTotalPortfolioValue(new BigDecimal("25000.00"));
        
        GetAccountPortfolioResponseDTO account2 = new GetAccountPortfolioResponseDTO();
        account2.setAccountId("ACC-002");
        account2.setTotalPortfolioValue(new BigDecimal("25000.00"));
        
        accounts.add(account1);
        accounts.add(account2);
        dto.setAccounts(accounts);

        assertEquals(2, dto.getAccounts().size());
        assertEquals("ACC-001", dto.getAccounts().get(0).getAccountId());
        assertEquals("ACC-002", dto.getAccounts().get(1).getAccountId());
    }

    @Test
    @DisplayName("Should handle very small decimal values")
    void testVerySmallDecimalValues() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        BigDecimal smallValue = new BigDecimal("0.0001");

        dto.setTotalGainLossPercent(smallValue);

        assertEquals(smallValue, dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should handle client ID with special characters")
    void testClientIdWithSpecialCharacters() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();
        String specialClientId = UUID.randomUUID().toString() + "-special";

        dto.setClientId(specialClientId);

        assertEquals(specialClientId, dto.getClientId());
    }

    @Test
    @DisplayName("Should maintain consistency between constructor and getters")
    void testConsistencyBetweenConstructorAndGetters() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO(
            clientId, accounts, totalPortfolioValue,
            totalCostBasis, totalGainLossDollars, totalGainLossPercent
        );

        // Verify all values are correctly set through constructor
        assertEquals(clientId, dto.getClientId());
        assertEquals(accounts, dto.getAccounts());
        assertEquals(0, totalPortfolioValue.compareTo(dto.getTotalPortfolioValue()));
        assertEquals(0, totalCostBasis.compareTo(dto.getTotalCostBasis()));
        assertEquals(0, totalGainLossDollars.compareTo(dto.getTotalGainLossDollars()));
        assertEquals(0, totalGainLossPercent.compareTo(dto.getTotalGainLossPercent()));
    }

    @Test
    @DisplayName("Should handle updating all fields independently")
    void testUpdateFieldsIndependently() {
        EntirePortfolioResponseDTO dto = new EntirePortfolioResponseDTO();

        // Set each field independently and verify
        dto.setClientId(clientId);
        assertEquals(clientId, dto.getClientId());

        dto.setAccounts(accounts);
        assertEquals(accounts, dto.getAccounts());

        dto.setTotalPortfolioValue(totalPortfolioValue);
        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());

        // Verify other fields are still null
        assertNull(dto.getTotalCostBasis());
        assertNull(dto.getTotalGainLossDollars());
    }
}
