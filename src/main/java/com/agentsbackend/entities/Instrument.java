package com.agentsbackend.entities;

import jakarta.persistence.*;
import com.agentsbackend.enums.AssetClass;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @Column(name = "instrument_id", columnDefinition = "UUID")
    private UUID instrumentId;

    @Column(name = "ticker", length = 10, nullable = false, unique = true)
    private String ticker;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "asset_class", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;

    @Column(name = "industry", length = 100)
    private String industry;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstrumentPrice> prices;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL)
    private List<Holding> holdings;

    public Instrument() {
    }

    public Instrument(UUID instrumentId, String ticker, String name, AssetClass assetClass,
                      String industry, List<InstrumentPrice> prices, List<Order> orders,
                      List<Holding> holdings) {
        this.instrumentId = instrumentId;
        this.ticker = ticker;
        this.name = name;
        this.assetClass = assetClass;
        this.industry = industry;
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
}
