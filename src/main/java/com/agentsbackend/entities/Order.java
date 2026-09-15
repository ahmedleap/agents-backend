package com.agentsbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(name = "order_id", columnDefinition = "UUID")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "order_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6, columnDefinition = "NUMERIC(18,6) CHECK (quantity > 0)")
    private BigDecimal quantity;

    @Column(name = "limit_price", nullable = false, precision = 18, scale = 4, columnDefinition = "NUMERIC(18,4) CHECK (limit_price > 0)")
    private BigDecimal limitPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
