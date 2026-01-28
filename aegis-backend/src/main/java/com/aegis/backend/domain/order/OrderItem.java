package com.aegis.backend.domain.order;

import com.aegis.backend.shared.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference // evita recursão JSON (se usar Jackson)
    private Order order;

    public OrderItem(String name, BigDecimal unitPrice, Integer quantity) {
        validate(unitPrice, quantity);
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    private void validate(BigDecimal unitPrice, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Item quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Unit price cannot be negative");
        }
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
