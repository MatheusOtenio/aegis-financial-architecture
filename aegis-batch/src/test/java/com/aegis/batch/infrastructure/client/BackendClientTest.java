package com.aegis.batch.infrastructure.client;

import com.aegis.batch.shared.dto.DailyReportDto;
import com.aegis.batch.shared.dto.OrderDto;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WireMockTest(httpPort = 8089)
class BackendClientTest {

    @Autowired
    private BackendClient backendClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aegis.backend.url", () -> "http://localhost:8089");
        registry.add("aegis.backend.token", () -> "test-token");
    }

    // --- Testes de Leitura (GET) ---

    @Test
    void shouldStreamOrdersSuccessfully() {
        stubFor(get(urlPathEqualTo("/internal/orders/daily"))
                .withQueryParam("date", equalTo("2026-01-28"))
                .withHeader("X-SERVICE-TOKEN", equalTo("test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"id": "uuid-1", "createdAt": "2026-01-28T10:00:00", "status": "PAID", "totalAmount": 100.00},
                                  {"id": "uuid-2", "createdAt": "2026-01-28T11:00:00", "status": "CREATED", "totalAmount": 50.50}
                                ]
                                """)));

        Flux<OrderDto> ordersFlux = backendClient.streamOrdersByDate(LocalDate.of(2026, 1, 28));

        StepVerifier.create(ordersFlux)
                .expectNextMatches(o -> o.id().equals("uuid-1") && o.totalAmount().intValue() == 100)
                .expectNextMatches(o -> o.id().equals("uuid-2") && o.status().equals("CREATED"))
                .verifyComplete();
    }

    // --- Testes de Escrita (POST) ---

    @Test
    void shouldPostReportSuccessfully() {
        DailyReportDto report = new DailyReportDto(LocalDate.now(), 10L, new BigDecimal("500.00"));

        stubFor(post(urlEqualTo("/internal/reports/daily"))
                .withHeader("X-SERVICE-TOKEN", equalTo("test-token"))
                .willReturn(aResponse().withStatus(201)));

        StepVerifier.create(backendClient.postDailyReport(report))
                .verifyComplete();

        verify(postRequestedFor(urlEqualTo("/internal/reports/daily")));
    }

    @Test
    void shouldHandleIdempotencyOn400() {
        DailyReportDto report = new DailyReportDto(LocalDate.now(), 10L, new BigDecimal("500.00"));

        // Simula Backend dizendo "Já existe" (400)
        stubFor(post(urlEqualTo("/internal/reports/daily"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Report already exists")));

        // Deve completar sem erro, pois o cliente trata o 400 como sucesso de negócio (idempotência)
        StepVerifier.create(backendClient.postDailyReport(report))
                .verifyComplete();
    }

    @Test
    void shouldRetryOn500() {
        DailyReportDto report = new DailyReportDto(LocalDate.now(), 10L, new BigDecimal("500.00"));

        // Configuração de Cenário para simular estado (Máquina de Estados do WireMock)
        stubFor(post(urlEqualTo("/internal/reports/daily"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 2")
        );

        stubFor(post(urlEqualTo("/internal/reports/daily"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Success")
        );

        stubFor(post(urlEqualTo("/internal/reports/daily"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Success")
                .willReturn(aResponse().withStatus(201))
        );

        // O StepVerifier aguarda a conclusão após os retries internos do WebClient/Resilience4j
        StepVerifier.create(backendClient.postDailyReport(report))
                .verifyComplete();

        // Verifica se o WireMock recebeu exatamente 3 chamadas
        verify(3, postRequestedFor(urlEqualTo("/internal/reports/daily")));
    }
}