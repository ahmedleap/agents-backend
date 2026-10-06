package com.agentsbackend.entities;

import com.agentsbackend.enums.AssetClass;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Instrument {

    private UUID instrumentId;
    private String ticker;
    private String name;
    private AssetClass assetClass;
    private String industry;
    private BigDecimal bid;
    private BigDecimal ask;
    private OffsetDateTime priceUpdatedAt;
    private List<InstrumentPrice> prices;
    private List<Order> orders;
    private List<Holding> holdings;

    public Instrument() {
    }

    public Instrument(UUID instrumentId, String ticker, String name, AssetClass assetClass,
                      String industry, BigDecimal bid, BigDecimal ask, OffsetDateTime priceUpdatedAt,
                      List<InstrumentPrice> prices, List<Order> orders,
                      List<Holding> holdings) {
        this.instrumentId = instrumentId;
        this.ticker = ticker;
        this.name = name;
        this.assetClass = assetClass;
        this.industry = industry;
        this.bid = bid;
        this.ask = ask;
        this.priceUpdatedAt = priceUpdatedAt;
        this.prices = prices;
        this.orders = orders;
        this.holdings = holdings;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClass assetClass) {
        this.assetClass = assetClass;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public List<InstrumentPrice> getPrices() {
        return prices;
    }

    public void setPrices(List<InstrumentPrice> prices) {
        this.prices = prices;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public OffsetDateTime getPriceUpdatedAt() {
        return priceUpdatedAt;
    }

    public void setPriceUpdatedAt(OffsetDateTime priceUpdatedAt) {
        this.priceUpdatedAt = priceUpdatedAt;
    }
}
