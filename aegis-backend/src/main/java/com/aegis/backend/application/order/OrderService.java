package com.aegis.backend.application.order;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderItem;
import com.aegis.backend.infrastructure.persistence.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public record CreateOrderCmd(String customerName, Integer tableNumber, List<OrderItemCmd> items) {}
    public record OrderItemCmd(String name, Integer quantity, BigDecimal unitPrice) {}

    @Transactional
    public UUID createOrder(CreateOrderCmd cmd) {
        // Regra: Calcular total no backend
        BigDecimal total = cmd.items.stream()
            .map(i -> i.unitPrice.multiply(BigDecimal.valueOf(i.quantity)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(UUID.randomUUID(), cmd.customerName, cmd.tableNumber, total);
        
        // Converter itens
        for (OrderItemCmd itemCmd : cmd.items) {
            OrderItem item = new OrderItem(UUID.randomUUID(), itemCmd.name, itemCmd.quantity, itemCmd.unitPrice);
            order.addItem(item);
        }

        repository.save(order);
        return order.getId();
    }
}
