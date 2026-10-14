package com.agentsbackend.DTO.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GetAccountPortfolioResponseDTO Tests")
class GetAccountPortfolioResponseDTOTest {

    private String accountId;
    private String accountName;
    private List<GetHoldingResponseDTO> holdings;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalGainLossDollars;
    private BigDecimal totalGainLossPercent;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID().toString();
        accountName = "Test Account";
        holdings = new ArrayList<>();
        totalPortfolioValue = new BigDecimal("10000.00");
        totalCostBasis = new BigDecimal("9000.00");
        totalGainLossDollars = new BigDecimal("1000.00");
        totalGainLossPercent = new BigDecimal("11.11");
    }

    @Test
    @DisplayName("Should create DTO with no-arg constructor")
    void testNoArgConstructor() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();

        assertNotNull(dto);
        assertNull(dto.getAccountId());
        assertNull(dto.getAccountName());
        assertNull(dto.getHoldings());
        assertNull(dto.getTotalPortfolioValue());
        assertNull(dto.getTotalCostBasis());
        assertNull(dto.getTotalGainLossDollars());
        assertNull(dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should create DTO with full constructor")
    void testFullConstructor() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO(
            accountId, accountName, holdings, totalPortfolioValue,
            totalCostBasis, totalGainLossDollars, totalGainLossPercent
        );

        assertEquals(accountId, dto.getAccountId());
        assertEquals(accountName, dto.getAccountName());
        assertEquals(holdings, dto.getHoldings());
        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
        assertEquals(totalCostBasis, dto.getTotalCostBasis());
        assertEquals(totalGainLossDollars, dto.getTotalGainLossDollars());
        assertEquals(totalGainLossPercent, dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should set and get accountId")
    void testSetAndGetAccountId() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setAccountId(accountId);

        assertEquals(accountId, dto.getAccountId());
    }

    @Test
    @DisplayName("Should set and get accountName")
    void testSetAndGetAccountName() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setAccountName(accountName);

        assertEquals(accountName, dto.getAccountName());
    }

    @Test
    @DisplayName("Should set and get holdings")
    void testSetAndGetHoldings() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holdings.add(holding);

        dto.setHoldings(holdings);

        assertEquals(holdings, dto.getHoldings());
        assertEquals(1, dto.getHoldings().size());
    }

    @Test
    @DisplayName("Should set and get totalPortfolioValue")
    void testSetAndGetTotalPortfolioValue() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setTotalPortfolioValue(totalPortfolioValue);

        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should set and get totalCostBasis")
    void testSetAndGetTotalCostBasis() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setTotalCostBasis(totalCostBasis);

        assertEquals(totalCostBasis, dto.getTotalCostBasis());
    }

    @Test
    @DisplayName("Should set and get totalGainLossDollars")
    void testSetAndGetTotalGainLossDollars() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setTotalGainLossDollars(totalGainLossDollars);

        assertEquals(totalGainLossDollars, dto.getTotalGainLossDollars());
    }

    @Test
    @DisplayName("Should set and get totalGainLossPercent")
    void testSetAndGetTotalGainLossPercent() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setTotalGainLossPercent(totalGainLossPercent);

        assertEquals(totalGainLossPercent, dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should handle null holdings list")
    void testSetAndGetNullHoldings() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        dto.setHoldings(null);

        assertNull(dto.getHoldings());
    }

    @Test
    @DisplayName("Should handle empty holdings list")
    void testSetAndGetEmptyHoldings() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        List<GetHoldingResponseDTO> emptyList = new ArrayList<>();
        dto.setHoldings(emptyList);

        assertNotNull(dto.getHoldings());
        assertTrue(dto.getHoldings().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple holdings")
    void testMultipleHoldings() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        GetHoldingResponseDTO holding1 = new GetHoldingResponseDTO();
        GetHoldingResponseDTO holding2 = new GetHoldingResponseDTO();
        holdings.add(holding1);
        holdings.add(holding2);

        dto.setHoldings(holdings);

        assertEquals(2, dto.getHoldings().size());
    }

    @Test
    @DisplayName("Should handle large portfolio values")
    void testLargePortfolioValues() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        BigDecimal largeValue = new BigDecimal("1000000.00");

        dto.setTotalPortfolioValue(largeValue);

        assertEquals(largeValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should handle zero portfolio values")
    void testZeroPortfolioValues() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        BigDecimal zeroValue = BigDecimal.ZERO;

        dto.setTotalPortfolioValue(zeroValue);
        dto.setTotalGainLossDollars(zeroValue);

        assertEquals(zeroValue, dto.getTotalPortfolioValue());
        assertEquals(zeroValue, dto.getTotalGainLossDollars());
    }

    @Test
    @DisplayName("Should handle negative gain loss values")
    void testNegativeGainLossValues() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        BigDecimal negativeValue = new BigDecimal("-500.00");

        dto.setTotalGainLossDollars(negativeValue);
        dto.setTotalGainLossPercent(new BigDecimal("-5.55"));

        assertEquals(negativeValue, dto.getTotalGainLossDollars());
        assertEquals(new BigDecimal("-5.55"), dto.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should update multiple fields")
    void testUpdateMultipleFields() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();

        dto.setAccountId(accountId);
        dto.setAccountName(accountName);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setTotalCostBasis(totalCostBasis);

        assertEquals(accountId, dto.getAccountId());
        assertEquals(accountName, dto.getAccountName());
        assertEquals(totalPortfolioValue, dto.getTotalPortfolioValue());
        assertEquals(totalCostBasis, dto.getTotalCostBasis());
    }

    @Test
    @DisplayName("Should overwrite previous values")
    void testOverwriteValues() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        String oldAccountId = "old-account-id";
        String newAccountId = "new-account-id";

        dto.setAccountId(oldAccountId);
        assertEquals(oldAccountId, dto.getAccountId());

        dto.setAccountId(newAccountId);
        assertEquals(newAccountId, dto.getAccountId());
    }

    @Test
    @DisplayName("Should handle precision in BigDecimal fields")
    void testPrecisionInBigDecimalFields() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        BigDecimal preciseValue = new BigDecimal("1234.5678");

        dto.setTotalPortfolioValue(preciseValue);

        assertEquals(preciseValue, dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should construct with all null values in full constructor")
    void testFullConstructorWithNullValues() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO(
            null, null, null, null, null, null, null
        );

        assertNull(dto.getAccountId());
        assertNull(dto.getAccountName());
        assertNull(dto.getHoldings());
        assertNull(dto.getTotalPortfolioValue());
    }

    @Test
    @DisplayName("Should preserve holding objects in list")
    void testPreserveHoldingObjects() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setTicker("AAPL");
        holdings.add(holding);

        dto.setHoldings(holdings);

        assertEquals("AAPL", dto.getHoldings().get(0).getTicker());
    }

    @Test
    @DisplayName("Should handle account name with special characters")
    void testAccountNameWithSpecialCharacters() {
        GetAccountPortfolioResponseDTO dto = new GetAccountPortfolioResponseDTO();
        String specialName = "Account @#$% 123!";

        dto.setAccountName(specialName);

        assertEquals(specialName, dto.getAccountName());
    }
}
