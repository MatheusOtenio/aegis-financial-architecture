package com.aegis.backend;

import com.aegis.backend.application.order.OrderService.CreateOrderCmd;
import com.aegis.backend.application.order.OrderService.OrderItemCmd;
import com.aegis.backend.application.report.ReportService.DailyReportDTO;
import com.aegis.backend.application.report.ReportService.TopItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Importante: Faz rollback no banco após cada teste para não sujar o ambiente
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrderPublicly() throws Exception {
        var item = new OrderItemCmd("Burger", 2, new BigDecimal("15.50"));
        var cmd = new CreateOrderCmd("John Doe", 10, List.of(item));

        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void shouldRejectInternalReportWithoutToken() throws Exception {
        var dto = new DailyReportDTO(LocalDate.now(), 10, new BigDecimal("100.00"), List.of());

        mvc.perform(post("/internal/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAcceptInternalReportWithToken() throws Exception {
        // Usa uma data futura para evitar colisão com dados reais do dia
        var dto = new DailyReportDTO(
                LocalDate.now().plusDays(500),
                5,
                new BigDecimal("50.00"),
                List.of(new TopItemDTO("Coke", 5))
        );

        mvc.perform(post("/internal/reports")
                .header("X-SERVICE-TOKEN", "dev-secret-token-123") // Token configurado no application.yml
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}
