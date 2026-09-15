package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.agentsbackend.enums.AssetClass;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instruments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
