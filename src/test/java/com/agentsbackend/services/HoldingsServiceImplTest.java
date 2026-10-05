package com.agentsbackend.services;

import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Holding;
import com.agentsbackend.entities.Instrument;
import com.agentsbackend.entities.Order;
import com.agentsbackend.repos.HoldingsRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingsServiceImpl Tests")
class HoldingsServiceImplTest {

    @Mock
    private HoldingsRepository holdingsRepository;

    @InjectMocks
    private HoldingsServiceImpl holdingsService;

    private UUID accountId;
    private UUID instrumentId;
    private UUID orderId;
    private Order order;
    private Account account;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        account = new Account();
        account.setAccountId(accountId);

        instrument = new Instrument();
        instrument.setInstrumentId(instrumentId);

        order = new Order();
        order.setOrderId(orderId);
        order.setAccount(account);
        order.setInstrument(instrument);
        order.setQuantity(new BigDecimal("100.00"));
    }

    // ==================== BUY ORDER TESTS ====================

    @Test
    @DisplayName("Should create new holding when buying and no holding exists")
    void testUpdateHoldingsForBuy_CreateNewHolding() {
        BigDecimal filledPrice = new BigDecimal("50.00");
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        holdingsService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

        Holding savedHolding = captor.getValue();
        assertNotNull(savedHolding.getHoldingId());
        assertEquals(new BigDecimal("100.00"), savedHolding.getQuantity());
        assertEquals(filledPrice, savedHolding.getAverageCostBasis());
        assertEquals(accountId, savedHolding.getAccount().getAccountId());
        assertEquals(instrumentId, savedHolding.getInstrument().getInstrumentId());
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
        existingHolding.setAccount(account);
        existingHolding.setInstrument(instrument);

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(existingHolding);

        holdingsService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

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
        order.setQuantity(new BigDecimal("50.25"));
        BigDecimal filledPrice = new BigDecimal("75.50");

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        holdingsService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

        Holding savedHolding = captor.getValue();
        assertEquals(new BigDecimal("50.25"), savedHolding.getQuantity());
        assertEquals(filledPrice, savedHolding.getAverageCostBasis());
    }

    @Test
    @DisplayName("Should handle high precision prices in buy order")
    void testUpdateHoldingsForBuy_HighPrecisionPrice() {
        BigDecimal filledPrice = new BigDecimal("123.456789");

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        holdingsService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

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
        holding.setAccount(account);
        holding.setInstrument(instrument);

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        // Second buy: 50 shares at $60
        order.setQuantity(new BigDecimal("50.00"));
        BigDecimal filledPrice = new BigDecimal("60.00");

        holdingsService.updateHoldingsForBuy(order, filledPrice);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

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

        order.setQuantity(new BigDecimal("100.00"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        verify(holdingsRepository).deleteByAccountAndInstrument(accountId, instrumentId);
        verify(holdingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update holding quantity when selling partial position")
    void testUpdateHoldingsForSell_UpdateQuantityPartialSell() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(account);
        holding.setInstrument(instrument);

        order.setQuantity(new BigDecimal("30.00"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("70.00"), updatedHolding.getQuantity());
        assertEquals(new BigDecimal("50.00"), updatedHolding.getAverageCostBasis());
        verify(holdingsRepository, never()).deleteByAccountAndInstrument(any(), any());
    }

    @Test
    @DisplayName("Should handle selling fractional shares")
    void testUpdateHoldingsForSell_FractionalShares() {
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.75"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(account);
        holding.setInstrument(instrument);

        order.setQuantity(new BigDecimal("25.25"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("75.50"), updatedHolding.getQuantity());
    }

    @Test
    @DisplayName("Should not update when selling and holding does not exist")
    void testUpdateHoldingsForSell_HoldingNotFound() {
        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(null);

        holdingsService.updateHoldingsForSell(order);

        verify(holdingsRepository, never()).save(any());
        verify(holdingsRepository, never()).deleteByAccountAndInstrument(any(), any());
    }

    @Test
    @DisplayName("Should handle multiple sells reducing position over time")
    void testUpdateHoldingsForSell_MultipleSells() {
        // Initial holding: 100 shares
        Holding holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setQuantity(new BigDecimal("100.00"));
        holding.setAverageCostBasis(new BigDecimal("50.00"));
        holding.setAccount(account);
        holding.setInstrument(instrument);

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        // First sell: 30 shares
        order.setQuantity(new BigDecimal("30.00"));
        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());
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
        holding.setAccount(account);
        holding.setInstrument(instrument);

        order.setQuantity(new BigDecimal("80.00"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

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
        holding.setAccount(account);
        holding.setInstrument(instrument);

        order.setQuantity(new BigDecimal("0.01"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

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
        holding.setAccount(account);
        holding.setInstrument(instrument);

        order.setQuantity(new BigDecimal("20.00"));

        when(holdingsRepository.findByAccountAndInstrument(accountId, instrumentId))
                .thenReturn(holding);

        holdingsService.updateHoldingsForSell(order);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingsRepository).save(captor.capture());

        Holding updatedHolding = captor.getValue();
        assertEquals(new BigDecimal("30.00"), updatedHolding.getQuantity());
        assertEquals(BigDecimal.ZERO, updatedHolding.getAverageCostBasis());
    }
}
