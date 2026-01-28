package com.aegis.backend.domain.order;

import com.aegis.backend.shared.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @JsonManagedReference // evita recursão JSON (se usar Jackson)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    public Order(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new DomainException("Order must have at least one item");
        }
        this.items = items;
        for (OrderItem item : items) {
            item.setOrder(this);
        }
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
        recalculateTotal();
    }

    // Helper para adicionar item individualmente e garantir consistência
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void pay() {
        if (this.status != OrderStatus.CREATED) {
            throw new DomainException("Only CREATED orders can be paid. Current status: " + this.status);
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (this.status == OrderStatus.PAID) {
            throw new DomainException("Cannot cancel a PAID order");
        }
        if (this.status == OrderStatus.CANCELED) {
            throw new DomainException("Order is already canceled");
        }
        this.status = OrderStatus.CANCELED;
    }
}
