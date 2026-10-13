package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardResponse {

    private List<AccountsResponse.AccountListItem> accounts;
    private List<MarketMover> marketMovers;
    private List<WatchlistResponse> watchlist;
    private List<RecentTrade> recentTrades;
    private List<PortfolioHistoryPoint> portfolioHistory;

    public DashboardResponse() {
    }

    public DashboardResponse(List<AccountsResponse.AccountListItem> accounts,
                             List<MarketMover> marketMovers,
                             List<WatchlistResponse> watchlist,
                             List<RecentTrade> recentTrades,
                             List<PortfolioHistoryPoint> portfolioHistory) {
        this.accounts = accounts;
        this.marketMovers = marketMovers;
        this.watchlist = watchlist;
        this.recentTrades = recentTrades;
        this.portfolioHistory = portfolioHistory;
    }

    public List<AccountsResponse.AccountListItem> getAccounts() { return accounts; }
    public void setAccounts(List<AccountsResponse.AccountListItem> accounts) { this.accounts = accounts; }
    public List<MarketMover> getMarketMovers() { return marketMovers; }
    public void setMarketMovers(List<MarketMover> marketMovers) { this.marketMovers = marketMovers; }
    public List<WatchlistResponse> getWatchlist() { return watchlist; }
    public void setWatchlist(List<WatchlistResponse> watchlist) { this.watchlist = watchlist; }
    public List<RecentTrade> getRecentTrades() { return recentTrades; }
    public void setRecentTrades(List<RecentTrade> recentTrades) { this.recentTrades = recentTrades; }
    public List<PortfolioHistoryPoint> getPortfolioHistory() { return portfolioHistory; }
    public void setPortfolioHistory(List<PortfolioHistoryPoint> portfolioHistory) { this.portfolioHistory = portfolioHistory; }

    public static class MarketMover {
        private String ticker;
        private String instrumentName;
        private BigDecimal currentPrice;
        private BigDecimal priceChange;
        private BigDecimal priceChangePercent;

        public MarketMover() {}

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getInstrumentName() { return instrumentName; }
        public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getPriceChange() { return priceChange; }
        public void setPriceChange(BigDecimal priceChange) { this.priceChange = priceChange; }
        public BigDecimal getPriceChangePercent() { return priceChangePercent; }
        public void setPriceChangePercent(BigDecimal priceChangePercent) { this.priceChangePercent = priceChangePercent; }
    }

    public static class RecentTrade {
        private UUID orderId;
        private UUID accountId;
        private String accountName;
        private String ticker;
        private String instrumentName;
        private com.agentsbackend.enums.OrderType orderType;
        private BigDecimal quantity;
        private BigDecimal limitPrice;
        private BigDecimal filledPrice;
        private com.agentsbackend.enums.OrderStatus status;
        private OffsetDateTime createdAt;

        public RecentTrade() {}

        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getInstrumentName() { return instrumentName; }
        public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
        public com.agentsbackend.enums.OrderType getOrderType() { return orderType; }
        public void setOrderType(com.agentsbackend.enums.OrderType orderType) { this.orderType = orderType; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getLimitPrice() { return limitPrice; }
        public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }
        public BigDecimal getFilledPrice() { return filledPrice; }
        public void setFilledPrice(BigDecimal filledPrice) { this.filledPrice = filledPrice; }
        public com.agentsbackend.enums.OrderStatus getStatus() { return status; }
        public void setStatus(com.agentsbackend.enums.OrderStatus status) { this.status = status; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class PortfolioHistoryPoint {
        private LocalDate date;
        private BigDecimal totalValue;

        public PortfolioHistoryPoint() {}

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    }
}