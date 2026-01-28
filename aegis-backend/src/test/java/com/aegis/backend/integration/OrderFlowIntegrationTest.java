package com.aegis.backend.integration;

import com.aegis.backend.domain.order.OrderStatus;
import com.aegis.backend.infrastructure.web.public_api.dto.CreateOrderRequest;
import com.aegis.backend.infrastructure.web.public_api.dto.OrderItemRequest;
import com.aegis.backend.infrastructure.web.public_api.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndPayOrder() {
        // 1. Criar Pedido
        OrderItemRequest item = new OrderItemRequest("Teclado Mecânico", new BigDecimal("350.00"), 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(item));

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/orders", request, OrderResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals(OrderStatus.CREATED, createResponse.getBody().status());
        assertEquals(new BigDecimal("350.00"), createResponse.getBody().totalAmount());

        var orderId = createResponse.getBody().id();

        // 2. Tentar Pagar
        ResponseEntity<Void> payResponse = restTemplate.postForEntity("/orders/" + orderId + "/pay", null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, payResponse.getStatusCode());

        // 3. Verificar estado atualizado (Listar)
        ResponseEntity<OrderResponse[]> listResponse = restTemplate.getForEntity("/orders", OrderResponse[].class);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());

        // Deve haver pelo menos 1 pedido PAGO
        boolean foundPaid = false;
        for (OrderResponse o : listResponse.getBody()) {
            if (o.id().equals(orderId) && o.status() == OrderStatus.PAID) {
                foundPaid = true;
                break;
            }
        }
        assertTrue(foundPaid, "Order should be updated to PAID");
    }

    @Test
    void shouldFailToCreateOrderWithInvalidData() {
        // Preço negativo não pode
        OrderItemRequest item = new OrderItemRequest("Erro", new BigDecimal("-10.00"), 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(item));

        ResponseEntity<Map> response = restTemplate.postForEntity("/orders", request, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Error", response.getBody().get("error"));
    }
}
