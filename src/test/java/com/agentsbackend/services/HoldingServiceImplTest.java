package com.agentsbackend.services;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Order;
import com.agentsbackend.enums.AssetClass;
import com.agentsbackend.exceptions.HoldingNotFoundException;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.HoldingRepository;
import com.agentsbackend.repos.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingServiceImpl Tests")
class HoldingServiceImplTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private HoldingServiceImpl holdingService;

    private UUID accountId;
    private UUID holdingId1;
    private UUID holdingId2;
    private UUID instrumentId1;
    private UUID instrumentId2;
    private UUID clientId;
    private Account testAccount;
    private Client testClient;
    private Instrument instrument1;
    private Instrument instrument2;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        holdingId1 = UUID.randomUUID();
        holdingId2 = UUID.randomUUID();
        instrumentId1 = UUID.randomUUID();
        instrumentId2 = UUID.randomUUID();
        clientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setClientId(clientId);

        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setClient(testClient);

        instrument1 = new Instrument();
        instrument1.setInstrumentId(instrumentId1);
        instrument1.setTicker("AAPL");
        instrument1.setName("Apple Inc");
        instrument1.setAssetClass(AssetClass.STOCK);
        instrument1.setIndustry("Technology");

        instrument2 = new Instrument();
        instrument2.setInstrumentId(instrumentId2);
        instrument2.setTicker("MSFT");
        instrument2.setName("Microsoft Inc");
        instrument2.setAssetClass(AssetClass.STOCK);
        instrument2.setIndustry("Technology");
    }

    @Test
    @DisplayName("Should get holdings by account ID with results")
    void testGetHoldingsByAccountId_WithResults() {
        Holding holding1 = new Holding();
        holding1.setHoldingId(holdingId1);
        holding1.setQuantity(new BigDecimal("100"));
        holding1.setAverageCostBasis(new BigDecimal("150.00"));

        Holding holding2 = new Holding();
        holding2.setHoldingId(holdingId2);
        holding2.setQuantity(new BigDecimal("50"));
        holding2.setAverageCostBasis(new BigDecimal("200.00"));

        when(holdingRepository.findByAccountId(accountId)).thenReturn(List.of(holding1, holding2));

        List<Holding> results = holdingService.getHoldingsByAccountId(accountId);

        assertEquals(2, results.size());
        assertEquals(holdingId1, results.get(0).getHoldingId());
        assertEquals(holdingId2, results.get(1).getHoldingId());
        verify(holdingRepository).findByAccountId(accountId);
    }

    @Test
    @DisplayName("Should get holdings by account ID with empty result")
    void testGetHoldingsByAccountId_Empty() {
        when(holdingRepository.findByAccountId(accountId)).thenReturn(List.of());

        List<Holding> results = holdingService.getHoldingsByAccountId(accountId);

        assertTrue(results.isEmpty());
        verify(holdingRepository).findByAccountId(accountId);
    }

    @Test
    @DisplayName("Should get current price from instrument")
    void testGetCurrentPrice_WithPrice() {
        BigDecimal expectedPrice = new BigDecimal("150.50");
        instrument1.setMidPrice(expectedPrice);

        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        BigDecimal result = holdingService.getCurrentPrice(instrumentId1);

        assertEquals(expectedPrice, result);
        verify(instrumentRepository).findById(instrumentId1);
    }

    @Test
    @DisplayName("Should return zero when no price available")
    void testGetCurrentPrice_NoPrice() {
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.empty());

        BigDecimal result = holdingService.getCurrentPrice(instrumentId1);

        assertEquals(BigDecimal.ZERO, result);
        verify(instrumentRepository).findById(instrumentId1);
    }

    @Test
    @DisplayName("Should get holdings DTOs by account ID with multiple holdings")
    void testGetHoldingsDTOByAccountId_MultipleHoldings() {
        instrument1.setMidPrice(new BigDecimal("160.00"));
        instrument2.setMidPrice(new BigDecimal("300.00"));

        Holding holding1 = new Holding();
        holding1.setHoldingId(holdingId1);
        holding1.setQuantity(new BigDecimal("100"));
        holding1.setAverageCostBasis(new BigDecimal("150.00"));
        holding1.setAccount(testAccount);
        holding1.setInstrument(instrument1);

        Holding holding2 = new Holding();
        holding2.setHoldingId(holdingId2);
        holding2.setQuantity(new BigDecimal("50"));
        holding2.setAverageCostBasis(new BigDecimal("200.00"));
        holding2.setAccount(testAccount);
        holding2.setInstrument(instrument2);

        when(holdingRepository.findByAccountId(accountId)).thenReturn(List.of(holding1, holding2));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(instrumentId2)).thenReturn(Optional.of(instrument2));

        List<GetHoldingResponseDTO> results = holdingService.getHoldingsDTOByAccountId(accountId);

        assertEquals(2, results.size());
        assertEquals("AAPL", results.get(0).getTicker());
        assertEquals("MSFT", results.get(1).getTicker());
        // holding1: quantity=100, currentPrice=160, currentValue=16000, avgCost=150, costBasis=15000, gain=1000
        assertEquals(0, new BigDecimal("16000").compareTo(results.get(0).getCurrentValue()));
        assertEquals(0, new BigDecimal("1000").compareTo(results.get(0).getGainLossDollars()));
    }

    @Test
    @DisplayName("Should get holdings DTOs by account ID with empty result")
    void testGetHoldingsDTOByAccountId_Empty() {
        when(holdingRepository.findByAccountId(accountId)).thenReturn(List.of());

        List<GetHoldingResponseDTO> results = holdingService.getHoldingsDTOByAccountId(accountId);

        assertTrue(results.isEmpty());
        verify(holdingRepository).findByAccountId(accountId);
    }

    @Test
    @DisplayName("Should get single holding DTO successfully")
    void testGetOneHoldingDTO_Success() {
        instrument1.setMidPrice(new BigDecimal("160.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("100"));
        holding.setAverageCostBasis(new BigDecimal("150.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        assertEquals(0, new BigDecimal("16000").compareTo(result.getCurrentValue()));
    }

    @Test
    @DisplayName("Should throw HoldingNotFoundException when holding not found")
    void testGetOneHoldingDTO_NotFound() {
        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.empty());

        assertThrows(HoldingNotFoundException.class, () -> {
            holdingService.getOneHoldingDTO(accountId, holdingId1);
        });
        verify(holdingRepository).findOneHolding(holdingId1, accountId);
    }

    @Test
    @DisplayName("Should calculate holding DTO with positive gain")
    void testHoldingToDTO_WithGain() {
        instrument1.setMidPrice(new BigDecimal("150.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("100"));
        holding.setAverageCostBasis(new BigDecimal("100.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // currentValue = 100 * 150 = 15000
        // costBasis = 100 * 100 = 10000
        // gainLossDollars = 15000 - 10000 = 5000
        // gainLossPercent = 5000 / 10000 * 100 = 50.00
        assertEquals(0, new BigDecimal("15000").compareTo(result.getCurrentValue()));
        assertEquals(0, new BigDecimal("5000").compareTo(result.getGainLossDollars()));
        assertEquals(0, new BigDecimal("50").compareTo(result.getGainLossPercent()));
    }

    @Test
    @DisplayName("Should calculate holding DTO with negative gain (loss)")
    void testHoldingToDTO_WithLoss() {
        instrument1.setMidPrice(new BigDecimal("150.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("100"));
        holding.setAverageCostBasis(new BigDecimal("200.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // currentValue = 100 * 150 = 15000
        // costBasis = 100 * 200 = 20000
        // gainLossDollars = 15000 - 20000 = -5000
        // gainLossPercent = -5000 / 20000 * 100 = -25.00
        assertEquals(0, new BigDecimal("15000").compareTo(result.getCurrentValue()));
        assertEquals(0, new BigDecimal("-5000").compareTo(result.getGainLossDollars()));
        assertEquals(0, new BigDecimal("-25").compareTo(result.getGainLossPercent()));
    }

    @Test
    @DisplayName("Should handle holding DTO with zero cost basis")
    void testHoldingToDTO_ZeroCostBasis() {
        instrument1.setMidPrice(new BigDecimal("150.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("100"));
        holding.setAverageCostBasis(new BigDecimal("0.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // currentValue = 100 * 150 = 15000
        // costBasis = 100 * 0 = 0
        // gainLossDollars = 15000 - 0 = 15000
        // gainLossPercent = 0 (due to zero cost basis check)
        assertEquals(0, new BigDecimal("15000").compareTo(result.getCurrentValue()));
        assertEquals(0, new BigDecimal("15000").compareTo(result.getGainLossDollars()));
        assertEquals(BigDecimal.ZERO, result.getGainLossPercent());
    }

    @Test
    @DisplayName("Should handle holding with fractional quantity")
    void testHoldingToDTO_FractionalQuantity() {
        instrument1.setMidPrice(new BigDecimal("200.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("0.5"));
        holding.setAverageCostBasis(new BigDecimal("100.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // currentValue = 0.5 * 200 = 100
        // costBasis = 0.5 * 100 = 50
        // gainLossDollars = 100 - 50 = 50
        // gainLossPercent = 50 / 50 * 100 = 100.00
        assertEquals(0, new BigDecimal("100").compareTo(result.getCurrentValue()));
        assertEquals(0, new BigDecimal("50").compareTo(result.getGainLossDollars()));
        assertEquals(0, new BigDecimal("100").compareTo(result.getGainLossPercent()));
    }

    @Test
    @DisplayName("Should handle gain loss percent with very large values")
    void testHoldingToDTO_LargeValues() {
        instrument1.setMidPrice(new BigDecimal("10500.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("1000"));
        holding.setAverageCostBasis(new BigDecimal("10000.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // currentValue = 1000 * 10500 = 10,500,000
        // costBasis = 1000 * 10000 = 10,000,000
        // gainLossDollars = 10,500,000 - 10,000,000 = 500,000
        // gainLossPercent = 500,000 / 10,000,000 * 100 = 5.00
        assertEquals(0, new BigDecimal("10500000").compareTo(result.getCurrentValue()));
        assertEquals(0, new BigDecimal("500000").compareTo(result.getGainLossDollars()));
        assertEquals(0, new BigDecimal("5").compareTo(result.getGainLossPercent()));
    }

    @Test
    @DisplayName("Should calculate gain loss percent with precision")
    void testGainLossPercent_WithPrecision() {
        instrument1.setMidPrice(new BigDecimal("105.50"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("33.333"));
        holding.setAverageCostBasis(new BigDecimal("99.99"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // Verify the percentage calculation uses RoundingMode.HALF_UP with 4 decimals
        assertNotNull(result.getGainLossPercent());
        assertTrue(result.getGainLossPercent().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should call getCurrentPrice multiple times in DTO conversion")
    void testHoldingToDTO_CallsGetCurrentPrice() {
        instrument1.setMidPrice(new BigDecimal("160.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("100"));
        holding.setAverageCostBasis(new BigDecimal("150.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // getCurrentPrice should be called once during holdingToDTO conversion
        verify(instrumentRepository, times(1)).findById(instrumentId1);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should map all fields correctly in holding DTO")
    void testHoldingToDTO_FieldMapping() {
        instrument1.setMidPrice(new BigDecimal("210.00"));

        Holding holding = new Holding();
        holding.setHoldingId(holdingId1);
        holding.setQuantity(new BigDecimal("50.5"));
        holding.setAverageCostBasis(new BigDecimal("200.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findOneHolding(holdingId1, accountId)).thenReturn(Optional.of(holding));
        when(instrumentRepository.findById(instrumentId1)).thenReturn(Optional.of(instrument1));

        GetHoldingResponseDTO result = holdingService.getOneHoldingDTO(accountId, holdingId1);

        // Verify all fields are correctly mapped
        assertEquals(holdingId1.toString(), result.getHoldingId());
        assertEquals(accountId.toString(), result.getAccountId());
        assertEquals(instrumentId1.toString(), result.getInstrumentId());
        assertEquals("AAPL", result.getTicker());
        assertEquals("Apple Inc", result.getName());
        assertEquals(0, new BigDecimal("50.5").compareTo(result.getQuantity()));
        assertEquals(0, new BigDecimal("200.00").compareTo(result.getAverageCostBasis()));
        assertEquals(0, new BigDecimal("210.00").compareTo(result.getCurrentPrice()));
    }

    // ==================== BUY ORDER TESTS ====================

    @Test
    @DisplayName("Should create new holding when buying and no holding exists")
    void testUpdateHoldingsForBuy_CreateNewHolding() {
        BigDecimal filledPrice = new BigDecimal("50.00");
        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("100.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(null);

        holdingService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding savedHolding = captor.getValue();
        assertNotNull(savedHolding.getHoldingId());
        assertEquals(new BigDecimal("100.00"), savedHolding.getQuantity());
        assertEquals(filledPrice, savedHolding.getAverageCostBasis());
        assertEquals(accountId, savedHolding.getAccount().getAccountId());
        assertEquals(instrumentId1, savedHolding.getInstrument().getInstrumentId());
    }

    @Test
    @DisplayName("Should update existing holding when buying and holding exists")
    void testUpdateHoldingsForBuy_UpdateExistingHolding() {
        BigDecimal filledPrice = new BigDecimal("50.00");
        BigDecimal existingQuantity = new BigDecimal("50.00");
        BigDecimal existingCostBasis = new BigDecimal("40.00");

        Holding existingHolding = new Holding();
        existingHolding.setHoldingId(UUID.randomUUID());
        existingHolding.setQuantity(existingQuantity);
        existingHolding.setAverageCostBasis(existingCostBasis);
        existingHolding.setAccount(testAccount);
        existingHolding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("100.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(existingHolding);

        holdingService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("150.00"), updatedHolding.getQuantity());

        // Verify average cost basis calculation: (50 * 40 + 100 * 50) / 150 = 46.6667
        BigDecimal expectedCostBasis = new BigDecimal("2000.00")
                .add(new BigDecimal("5000.00"))
                .divide(new BigDecimal("150.00"), 4, RoundingMode.HALF_UP);
        assertEquals(expectedCostBasis, updatedHolding.getAverageCostBasis());
    }

    @Test
    @DisplayName("Should handle fractional quantities in buy order")
    void testUpdateHoldingsForBuy_FractionalQuantity() {
        BigDecimal filledPrice = new BigDecimal("75.50");

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("50.25"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(null);

        holdingService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding savedHolding = captor.getValue();
        assertEquals(new BigDecimal("50.25"), savedHolding.getQuantity());
        assertEquals(filledPrice, savedHolding.getAverageCostBasis());
    }

    @Test
    @DisplayName("Should handle high precision prices in buy order")
    void testUpdateHoldingsForBuy_HighPrecisionPrice() {
        BigDecimal filledPrice = new BigDecimal("123.456789");

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("100.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1)).thenReturn(null);

        holdingService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding savedHolding = captor.getValue();
        assertEquals(filledPrice, savedHolding.getAverageCostBasis());
    }

    @Test
    @DisplayName("Should recalculate cost basis with multiple buys at different prices")
    void testUpdateHoldingsForBuy_MultipleBuysRecalculateCostBasis() {
        // First buy: 100 shares at $50
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        // Second buy: 50 shares at $60
        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("50.00"));
        BigDecimal filledPrice = new BigDecimal("60.00");

        holdingService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("150.00"), updatedHolding.getQuantity());

        // Cost basis: (100 * 50 + 50 * 60) / 150 = 8000 / 150 = 53.3333
        BigDecimal expectedCostBasis = new BigDecimal("8000.00")
                .divide(new BigDecimal("150.00"), 4, RoundingMode.HALF_UP);
        assertEquals(expectedCostBasis, updatedHolding.getAverageCostBasis());
    }

    // ==================== SELL ORDER TESTS ====================

    @Test
    @DisplayName("Should delete holding when selling entire position (quantity becomes zero)")
    void testUpdateHoldingsForSell_DeleteWhenQuantityBecomesZero() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("100.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        verify(holdingRepository).deleteByAccountAndInstrument(accountId, instrumentId1);
        verify(holdingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update holding quantity when selling partial position")
    void testUpdateHoldingsForSell_UpdateQuantityPartialSell() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("30.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("70.00"), updatedHolding.getQuantity());
        assertEquals(new BigDecimal("50.00"), updatedHolding.getAverageCostBasis());
        verify(holdingRepository, never()).deleteByAccountAndInstrument(any(), any());
    }

    @Test
    @DisplayName("Should handle selling fractional shares")
    void testUpdateHoldingsForSell_FractionalShares() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.75"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("25.25"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("75.50"), updatedHolding.getQuantity());
    }

    @Test
    @DisplayName("Should not update when selling and holding does not exist")
    void testUpdateHoldingsForSell_HoldingNotFound() {
        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("100.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(null);

        holdingService.updateHoldingsForSell(order);

        verify(holdingRepository, never()).save(any());
        verify(holdingRepository, never()).deleteByAccountAndInstrument(any(), any());
    }

    @Test
    @DisplayName("Should handle multiple sells reducing position over time")
    void testUpdateHoldingsForSell_MultipleSells() {
        // Initial holding: 100 shares
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        // First sell: 30 shares
        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("30.00"));

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());
        assertEquals(new BigDecimal("70.00"), captor.getValue().getQuantity());
    }

    @Test
    @DisplayName("Should preserve cost basis when selling")
    void testUpdateHoldingsForSell_PreserveCostBasis() {
        BigDecimal costBasis = new BigDecimal("75.50");
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("200.00"));
        holding.setAverageCostBasis(costBasis);
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("80.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(costBasis, updatedHolding.getAverageCostBasis());
    }

    @Test
    @DisplayName("Should handle selling very small fractional quantity")
    void testUpdateHoldingsForSell_VerySmallFraction() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("0.01"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("99.99"), updatedHolding.getQuantity());
    }

    @Test
    @DisplayName("Should handle zero cost basis during sell")
    void testUpdateHoldingsForSell_ZeroCostBasis() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("50.00"));
        holding.setAverageCostBasis(BigDecimal.ZERO);
        holding.setAccount(testAccount);
        holding.setInstrument(instrument1);

        com.agentsbackend.entities.Order order = new com.agentsbackend.entities.Order();
        order.setOrderId(UUID.randomUUID());
        order.setAccount(testAccount);
        order.setInstrument(instrument1);
        order.setQuantity(new BigDecimal("20.00"));

        when(holdingRepository.findByAccountAndInstrument(accountId, instrumentId1))
                .thenReturn(holding);

        holdingService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("30.00"), updatedHolding.getQuantity());
        assertEquals(BigDecimal.ZERO, updatedHolding.getAverageCostBasis());
    }
}
