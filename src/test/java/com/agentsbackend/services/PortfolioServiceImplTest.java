package com.agentsbackend.services;

import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO;
import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.AssetClass;
import com.agentsbackend.exceptions.AccountNotFoundException;
import com.agentsbackend.exceptions.ClientNotFoundException;
import com.agentsbackend.exceptions.UnauthorizedAccountAccessException;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.ClientRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@DisplayName("Portfolio Service Tests")
class PortfolioServiceImplTest {

    private PortfolioServiceImpl portfolioService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingService holdingService;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private ClientRepository clientRepository;

    private UUID clientId;
    private UUID accountId;
    private UUID instrumentId1;
    private UUID instrumentId2;
    private Client testClient;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        portfolioService = new PortfolioServiceImpl(
            accountRepository,
            holdingService,
            instrumentRepository,
            clientRepository
        );

        clientId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        instrumentId1 = UUID.randomUUID();
        instrumentId2 = UUID.randomUUID();

        // Create test client
        testClient = new Client();
        testClient.setClientId(clientId);

        // Create test account
        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setName("Test Account");
        testAccount.setClient(testClient);
    }

    // ==================== getPortfolioByAccountId Tests ====================

    @Test
    @DisplayName("Should return portfolio with correct gain/loss calculations")
    void testGetPortfolioByAccountId_CorrectCalculations() {
        // Setup
        BigDecimal currentValue = new BigDecimal("100.00");
        BigDecimal averageCostBasis = new BigDecimal("90.00");
        BigDecimal quantity = new BigDecimal("10");

        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setQuantity(quantity);
        holding.setAverageCostBasis(averageCostBasis);
        holding.setCurrentValue(currentValue);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        // Execute
        GetAccountPortfolioResponseDTO result = portfolioService.getPortfolioByAccountId(clientId, accountId);

        // Verify
        assertEquals(currentValue, result.getTotalPortfolioValue());
        
        // Total cost = quantity * average cost basis = 10 * 90 = 900
        BigDecimal expectedTotalCost = quantity.multiply(averageCostBasis);
        assertEquals(expectedTotalCost, result.getTotalCostBasis());
        
        // Gain/Loss in dollars = 100 - 900 = -800
        BigDecimal expectedGainLoss = currentValue.subtract(expectedTotalCost);
        assertEquals(expectedGainLoss, result.getTotalGainLossDollars());
        
        // Gain/Loss percent = -800 / 900 * 100 = -88.89%
        BigDecimal expectedGainLossPercent = expectedGainLoss
            .divide(expectedTotalCost, 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        assertEquals(expectedGainLossPercent, result.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account doesn't exist")
    void testGetPortfolioByAccountId_AccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, 
            () -> portfolioService.getPortfolioByAccountId(clientId, accountId));
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccountAccessException when account doesn't belong to client")
    void testGetPortfolioByAccountId_UnauthorizedAccess() {
        UUID differentClientId = UUID.randomUUID();
        testAccount.getClient().setClientId(differentClientId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(UnauthorizedAccountAccessException.class,
            () -> portfolioService.getPortfolioByAccountId(clientId, accountId));
    }

    @Test
    @DisplayName("Should calculate positive gain with correct percentage")
    void testGetPortfolioByAccountId_PositiveGain() {
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setQuantity(new BigDecimal("10"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setCurrentValue(new BigDecimal("600.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        GetAccountPortfolioResponseDTO result = portfolioService.getPortfolioByAccountId(clientId, accountId);

        // Total cost = 10 * 50 = 500
        // Gain = 600 - 500 = 100
        // Gain % = 100 / 500 * 100 = 20%
        assertEquals(new BigDecimal("600.00"), result.getTotalPortfolioValue());
        assertEquals(new BigDecimal("500.00"), result.getTotalCostBasis());
        assertEquals(new BigDecimal("100.00"), result.getTotalGainLossDollars());
        assertEquals(new BigDecimal("20.0000"), result.getTotalGainLossPercent());
    }

    @Test
    @DisplayName("Should handle zero cost basis by returning zero gain/loss percent")
    void testGetPortfolioByAccountId_ZeroCostBasis() {
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setQuantity(new BigDecimal("10"));
        holding.setAverageCostBasis(new BigDecimal("0.00"));
        holding.setCurrentValue(new BigDecimal("100.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        GetAccountPortfolioResponseDTO result = portfolioService.getPortfolioByAccountId(clientId, accountId);

        // When cost basis is 0, gain/loss percent should be 0
        assertEquals(BigDecimal.ZERO, result.getTotalGainLossPercent());
    }

    // ==================== getEntirePortfolioByClientId Tests ====================

    @Test
    @DisplayName("Should aggregate multiple account portfolios correctly")
    void testGetEntirePortfolioByClientId_MultipleAccounts() {
        // Setup first account
        UUID accountId2 = UUID.randomUUID();
        Account account2 = new Account();
        account2.setAccountId(accountId2);
        account2.setName("Account 2");
        account2.setClient(testClient);

        GetHoldingResponseDTO holding1 = new GetHoldingResponseDTO();
        holding1.setQuantity(new BigDecimal("10"));
        holding1.setAverageCostBasis(new BigDecimal("50.00"));
        holding1.setCurrentValue(new BigDecimal("600.00"));

        GetHoldingResponseDTO holding2 = new GetHoldingResponseDTO();
        holding2.setQuantity(new BigDecimal("5"));
        holding2.setAverageCostBasis(new BigDecimal("100.00"));
        holding2.setCurrentValue(new BigDecimal("600.00"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount, account2));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(accountId2)).thenReturn(Optional.of(account2));
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding1));
        when(holdingService.getHoldingsDTOByAccountId(accountId2)).thenReturn(List.of(holding2));

        EntirePortfolioResponseDTO result = portfolioService.getEntirePortfolioByClientId(clientId);

        // Account 1: value=600, cost=500, gain=100
        // Account 2: value=600, cost=500, gain=100
        // Total: value=1200, cost=1000, gain=200
        assertEquals(new BigDecimal("1200.00"), result.getTotalPortfolioValue());
        assertEquals(new BigDecimal("1000.00"), result.getTotalCostBasis());
        assertEquals(new BigDecimal("200.00"), result.getTotalGainLossDollars());
        // Gain % = 200 / 1000 * 100 = 20%
        assertEquals(new BigDecimal("20.0000"), result.getTotalGainLossPercent());
        assertEquals(2, result.getAccounts().size());
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException when client doesn't exist")
    void testGetEntirePortfolioByClientId_ClientNotFound() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
            () -> portfolioService.getEntirePortfolioByClientId(clientId));
    }

    @Test
    @DisplayName("Should handle client with no accounts")
    void testGetEntirePortfolioByClientId_NoAccounts() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of());

        EntirePortfolioResponseDTO result = portfolioService.getEntirePortfolioByClientId(clientId);

        assertEquals(BigDecimal.ZERO, result.getTotalPortfolioValue());
        assertEquals(BigDecimal.ZERO, result.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, result.getTotalGainLossDollars());
        assertEquals(0, result.getAccounts().size());
    }

    // ==================== getPortfolioAllocation Tests ====================

    @Test
    @DisplayName("Should calculate allocation percentages correctly for single holding")
    void testGetPortfolioAllocation_SingleHolding() {
        // Setup
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.STOCK);
        instrument.setMidPrice(new BigDecimal("50.00"));

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100"));
        holding.setInstrument(instrument);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument));

        GetAllocationResponseDTO result = portfolioService.getPortfolioAllocation(clientId);

        // Total value = 100 * 50 = 5000
        // Technology industry = 100%, STOCK asset class = 100%
        assertEquals(1, result.getIndustryBreakdown().size());
        assertEquals(1, result.getAssetClassBreakdown().size());
        assertEquals("Technology", result.getIndustryBreakdown().get(0).getIndustry());
        assertEquals(new BigDecimal("100.0000"), result.getIndustryBreakdown().get(0).getPercentage());
        assertEquals(new BigDecimal("5000.00"), result.getIndustryBreakdown().get(0).getValue());
    }

    @Test
    @DisplayName("Should calculate allocation percentages correctly for multiple holdings")
    void testGetPortfolioAllocation_MultipleHoldings() {
        // Setup
        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentId(instrumentId1);
        instrument1.setIndustry("Technology");
        instrument1.setAssetClass(AssetClass.STOCK);

        Instrument instrument2 = new Instrument();
        instrument2.setInstrumentId(instrumentId2);
        instrument2.setIndustry("Finance");
        instrument2.setAssetClass(AssetClass.STOCK);

        Holding holding1 = new Holding();
        holding1.setHoldingId(UUID.randomUUID());
        holding1.setQuantity(new BigDecimal("100"));
        holding1.setInstrument(instrument1);

        Holding holding2 = new Holding();
        holding2.setHoldingId(UUID.randomUUID());
        holding2.setQuantity(new BigDecimal("100"));
        holding2.setInstrument(instrument2);

        Instrument instr1 = new Instrument();
        instr1.setInstrumentId(instrumentId1);
        instr1.setMidPrice(new BigDecimal("50.00"));
        
        Instrument instr2 = new Instrument();
        instr2.setInstrumentId(instrumentId2);
        instr2.setMidPrice(new BigDecimal("50.00"));
        
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding1, holding2));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instr1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(instr2));

        GetAllocationResponseDTO result = portfolioService.getPortfolioAllocation(clientId);

        // Total value = (100 * 50) + (100 * 50) = 10000
        // Technology = 5000 (50%), Finance = 5000 (50%)
        assertEquals(2, result.getIndustryBreakdown().size());
        assertEquals(new BigDecimal("50.0000"), result.getIndustryBreakdown().get(0).getPercentage());
        assertEquals(new BigDecimal("50.0000"), result.getIndustryBreakdown().get(1).getPercentage());
    }

    @Test
    @DisplayName("Should handle null industry by using 'Unknown'")
    void testGetPortfolioAllocation_NullIndustry() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry(null);
        instrument.setAssetClass(AssetClass.STOCK);
        instrument.setMidPrice(new BigDecimal("50.00"));

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100"));
        holding.setInstrument(instrument);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument));

        GetAllocationResponseDTO result = portfolioService.getPortfolioAllocation(clientId);

        assertEquals("Unknown", result.getIndustryBreakdown().get(0).getIndustry());
    }

    @Test
    @DisplayName("Should return empty allocation when client has no holdings")
    void testGetPortfolioAllocation_NoHoldings() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of());

        GetAllocationResponseDTO result = portfolioService.getPortfolioAllocation(clientId);

        assertEquals(0, result.getIndustryBreakdown().size());
        assertEquals(0, result.getAssetClassBreakdown().size());
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException when client doesn't exist")
    void testGetPortfolioAllocation_ClientNotFound() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
            () -> portfolioService.getPortfolioAllocation(clientId));
    }

    @Test
    @DisplayName("Should handle zero price by using zero in calculations")
    void testGetPortfolioAllocation_ZeroPrice() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.STOCK);

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100"));
        holding.setInstrument(instrument);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.empty());

        GetAllocationResponseDTO result = portfolioService.getPortfolioAllocation(clientId);

        // When total value is 0, should return empty lists
        assertEquals(0, result.getIndustryBreakdown().size());
        assertEquals(0, result.getAssetClassBreakdown().size());
    }

    // ==================== getAccountAllocation Tests ====================

    @Test
    @DisplayName("Should calculate account allocation percentages correctly")
    void testGetAccountAllocation_CorrectCalculations() {
        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentId(instrumentId1);
        instrument1.setIndustry("Technology");
        instrument1.setAssetClass(AssetClass.STOCK);

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100"));
        holding.setInstrument(instrument1);

        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setMidPrice(new BigDecimal("50.00"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Total value = 100 * 50 = 5000
        assertEquals(1, result.getIndustryBreakdown().size());
        assertEquals(new BigDecimal("100.0000"), result.getIndustryBreakdown().get(0).getPercentage());
        assertEquals(new BigDecimal("5000.00"), result.getIndustryBreakdown().get(0).getValue());
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccountAccessException for wrong client")
    void testGetAccountAllocation_UnauthorizedAccess() {
        UUID differentClientId = UUID.randomUUID();
        testAccount.getClient().setClientId(differentClientId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(UnauthorizedAccountAccessException.class,
            () -> portfolioService.getAccountAllocation(clientId, accountId));
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account doesn't exist")
    void testGetAccountAllocation_AccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
            () -> portfolioService.getAccountAllocation(clientId, accountId));
    }

    @Test
    @DisplayName("Should return empty allocation when account has no holdings")
    void testGetAccountAllocation_NoHoldings() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of());

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        assertEquals(0, result.getIndustryBreakdown().size());
        assertEquals(0, result.getAssetClassBreakdown().size());
    }

    // ==================== Allocation Percentage Accuracy Tests ====================

    @Test
    @DisplayName("Should calculate allocation percentages that sum to 100%")
    void testAllocationPercentageSum_SumTo100() {
        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentId(instrumentId1);
        instrument1.setIndustry("Technology");
        instrument1.setAssetClass(AssetClass.STOCK);

        Instrument instrument2 = new Instrument();
        instrument2.setInstrumentId(instrumentId2);
        instrument2.setIndustry("Finance");
        instrument2.setAssetClass(AssetClass.ETF);

        Holding holding1 = new Holding();
        holding1.setHoldingId(UUID.randomUUID());
        holding1.setQuantity(new BigDecimal("50"));
        holding1.setInstrument(instrument1);

        Holding holding2 = new Holding();
        holding2.setHoldingId(UUID.randomUUID());
        holding2.setQuantity(new BigDecimal("50"));
        holding2.setInstrument(instrument2);

        Instrument inst1 = new Instrument();
        inst1.setInstrumentId(instrumentId1);
        inst1.setMidPrice(new BigDecimal("100.00"));
        
        Instrument inst2 = new Instrument();
        inst2.setInstrumentId(instrumentId2);
        inst2.setMidPrice(new BigDecimal("100.00"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding1, holding2));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(inst1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(inst2));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        BigDecimal industrySum = result.getIndustryBreakdown().stream()
            .map(GetAllocationResponseDTO.IndustryAllocationDTO::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal assetClassSum = result.getAssetClassBreakdown().stream()
            .map(GetAllocationResponseDTO.AssetClassAllocationDTO::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("100.0000"), industrySum);
        assertEquals(new BigDecimal("100.0000"), assetClassSum);
    }

    @Test
    @DisplayName("Should calculate precise percentages with BigDecimal precision")
    void testAllocationPercentages_Precision() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.STOCK);
        instrument.setMidPrice(new BigDecimal("3.00"));

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("333"));
        holding.setInstrument(instrument);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Value = 333 * 3 = 999
        // Percentage = 999 / 999 * 100 = 100%
        assertEquals(new BigDecimal("100.0000"), result.getIndustryBreakdown().get(0).getPercentage());
    }

    // ==================== Calculation Helper Tests ====================

    @Test
    @DisplayName("Should handle fractional quantities in value calculation")
    void testValueCalculation_FractionalQuantity() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.ETF);

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("0.5"));
        holding.setInstrument(instrument);

        Instrument instr = new Instrument();
        instr.setInstrumentId(instrumentId1);
        instr.setMidPrice(new BigDecimal("1000.00"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instr));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Value = 0.5 * 1000 = 500 (may have precision 500.000)
        // Use compareTo() to compare value regardless of scale/representation
        assertEquals(0, new BigDecimal("500").compareTo(result.getIndustryBreakdown().get(0).getValue()));
    }

    @Test
    @DisplayName("Should handle multiple holdings with same industry")
    void testAllocation_MultipleHoldingsSameIndustry() {
        Instrument instrument1 = new Instrument();
        instrument1.setInstrumentId(instrumentId1);
        instrument1.setIndustry("Technology");
        instrument1.setAssetClass(AssetClass.STOCK);

        Instrument instrument2 = new Instrument();
        instrument2.setInstrumentId(instrumentId2);
        instrument2.setIndustry("Technology");
        instrument2.setAssetClass(AssetClass.ETF);

        Holding holding1 = new Holding();
        holding1.setHoldingId(UUID.randomUUID());
        holding1.setQuantity(new BigDecimal("100"));
        holding1.setInstrument(instrument1);

        Holding holding2 = new Holding();
        holding2.setHoldingId(UUID.randomUUID());
        holding2.setQuantity(new BigDecimal("100"));
        holding2.setInstrument(instrument2);

        Instrument ins1 = new Instrument();
        ins1.setInstrumentId(instrumentId1);
        ins1.setMidPrice(new BigDecimal("50.00"));
        
        Instrument ins2 = new Instrument();
        ins2.setInstrumentId(instrumentId2);
        ins2.setMidPrice(new BigDecimal("50.00"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding1, holding2));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(ins1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(ins2));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Both holdings in Technology industry
        // Total value = 5000 + 5000 = 10000
        // Technology = 10000 (100%)
        assertEquals(1, result.getIndustryBreakdown().size());
        assertEquals("Technology", result.getIndustryBreakdown().get(0).getIndustry());
        assertEquals(new BigDecimal("10000.00"), result.getIndustryBreakdown().get(0).getValue());
    }

    @Test
    @DisplayName("Should handle very small decimal values")
    void testAllocation_VerySmallValues() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.STOCK);

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("0.01"));
        holding.setInstrument(instrument);

        Instrument instSmall = new Instrument();
        instSmall.setInstrumentId(instrumentId1);
        instSmall.setMidPrice(new BigDecimal("0.001"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instSmall));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Value = 0.01 * 0.001 = 0.00001
        assertEquals(new BigDecimal("0.00001"), result.getIndustryBreakdown().get(0).getValue());
    }

    @Test
    @DisplayName("Should handle large currency values")
    void testAllocation_LargeValues() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId(instrumentId1);
        instrument.setIndustry("Technology");
        instrument.setAssetClass(AssetClass.STOCK);

        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("1000000"));
        holding.setInstrument(instrument);

        Instrument instLarge = new Instrument();
        instLarge.setInstrumentId(instrumentId1);
        instLarge.setMidPrice(new BigDecimal("10000.00"));
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instLarge));

        GetAllocationResponseDTO result = portfolioService.getAccountAllocation(clientId, accountId);

        // Value = 1000000 * 10000 = 10000000000
        assertEquals(new BigDecimal("10000000000.00"), result.getIndustryBreakdown().get(0).getValue());
    }

    @Test
    @DisplayName("Should verify repository method calls")
    void testRepositoryMethodCalls_Verify() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(holdingService.getHoldingsByAccountId(accountId)).thenReturn(List.of());

        portfolioService.getAccountAllocation(clientId, accountId);

        verify(accountRepository, times(1)).findById(accountId);
        verify(holdingService, times(1)).getHoldingsByAccountId(accountId);
    }
}
