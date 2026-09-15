package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instrument_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"instrument_id", "as_of"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentPrice {

    @Id
    @Column(name = "price_id", columnDefinition = "UUID")
    private UUID priceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "price", nullable = false, precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (price > 0)")
    private BigDecimal price;

    @Column(name = "as_of", nullable = false)
    private LocalDateTime asOf;
}
