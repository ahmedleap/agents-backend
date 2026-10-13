package com.agentsbackend.DTO.response;

import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardResponseTest {

    @Test
    void exposesDashboardSectionsAndNestedFields() {
        DashboardResponse response = new DashboardResponse();
        List<AccountsResponse.AccountListItem> accounts = List.of();
        List<DashboardResponse.MarketMover> movers = List.of();
        List<WatchlistResponse> watchlist = List.of();
        List<DashboardResponse.RecentTrade> trades = List.of();
        List<DashboardResponse.PortfolioHistoryPoint> history = List.of();

        response.setAccounts(accounts);
        response.setMarketMovers(movers);
        response.setWatchlist(watchlist);
        response.setRecentTrades(trades);
        response.setPortfolioHistory(history);
        assertEquals(accounts, response.getAccounts());
        assertEquals(movers, response.getMarketMovers());
        assertEquals(watchlist, response.getWatchlist());
        assertEquals(trades, response.getRecentTrades());
        assertEquals(history, response.getPortfolioHistory());

        DashboardResponse.MarketMover mover = new DashboardResponse.MarketMover();
        mover.setTicker("AAPL");
        mover.setInstrumentName("Apple Inc.");
        mover.setCurrentPrice(new BigDecimal("200.00"));
        mover.setPriceChange(new BigDecimal("2.00"));
        mover.setPriceChangePercent(new BigDecimal("1.00"));
        assertEquals("AAPL", mover.getTicker());
        assertEquals("Apple Inc.", mover.getInstrumentName());
        assertEquals(new BigDecimal("200.00"), mover.getCurrentPrice());
        assertEquals(new BigDecimal("2.00"), mover.getPriceChange());
        assertEquals(new BigDecimal("1.00"), mover.getPriceChangePercent());

        UUID orderId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        DashboardResponse.RecentTrade trade = new DashboardResponse.RecentTrade();
        trade.setOrderId(orderId);
        trade.setAccountId(accountId);
        trade.setAccountName("Brokerage");
        trade.setTicker("AAPL");
        trade.setInstrumentName("Apple Inc.");
        trade.setOrderType(OrderType.BUY);
        trade.setQuantity(new BigDecimal("2"));
        trade.setLimitPrice(new BigDecimal("199.00"));
        trade.setFilledPrice(new BigDecimal("198.50"));
        trade.setStatus(OrderStatus.FILLED);
        trade.setCreatedAt(createdAt);
        assertEquals(orderId, trade.getOrderId());
        assertEquals(accountId, trade.getAccountId());
        assertEquals("Brokerage", trade.getAccountName());
        assertEquals("AAPL", trade.getTicker());
        assertEquals("Apple Inc.", trade.getInstrumentName());
        assertEquals(OrderType.BUY, trade.getOrderType());
        assertEquals(new BigDecimal("2"), trade.getQuantity());
        assertEquals(new BigDecimal("199.00"), trade.getLimitPrice());
        assertEquals(new BigDecimal("198.50"), trade.getFilledPrice());
        assertEquals(OrderStatus.FILLED, trade.getStatus());
        assertEquals(createdAt, trade.getCreatedAt());

        DashboardResponse.PortfolioHistoryPoint point = new DashboardResponse.PortfolioHistoryPoint();
        point.setDate(LocalDate.of(2026, 10, 13));
        point.setTotalValue(new BigDecimal("1000.00"));
        assertEquals(LocalDate.of(2026, 10, 13), point.getDate());
        assertEquals(new BigDecimal("1000.00"), point.getTotalValue());
    }
}