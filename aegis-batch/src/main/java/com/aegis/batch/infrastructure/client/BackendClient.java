package com.aegis.batch.infrastructure.client;

import com.aegis.batch.shared.dto.DailyReportDto;
import com.aegis.batch.shared.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackendClient {

    private final WebClient backendWebClient;

    @Value("${aegis.job.retry.max-attempts:3}")
    private long maxRetryAttempts;

    @Value("${aegis.job.retry.backoff-ms:500}")
    private long retryBackoffMs;

    // --- LEITURA (Já existente) ---
    public Flux<OrderDto> streamOrdersByDate(LocalDate date) {
        log.info("Iniciando streaming de pedidos para a data: {}", date);
        return backendWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/orders/daily")
                        .queryParam("date", date.toString())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(OrderDto.class)
                .doOnError(e -> log.error("Erro ao fazer streaming de pedidos: {}", e.getMessage()));
    }

    // --- ESCRITA (Novo) ---
    public Mono<Void> postDailyReport(DailyReportDto report) {
        log.info("Enviando relatório para o backend. Data: {}", report.date());

        return backendWebClient.post()
                .uri("/internal/reports/daily")
                .bodyValue(report)
                .retrieve()
                .toBodilessEntity()
                .then() // Converte para Mono<Void> se sucesso (2xx)
                .doOnSuccess(v -> log.info("Relatório enviado com sucesso!"))
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Tratamento Especial: 400 Bad Request (Pode ser Idempotência)
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        log.warn("Backend retornou 400. Assumindo idempotência/já processado: {}", e.getResponseBodyAsString());
                        return Mono.empty(); // Sucesso lógico, não falha o job
                    }
                    // 403 Forbidden -> Erro Fatal, propaga
                    if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.error("Erro de Autenticação (403). Verifique o Token!");
                        return Mono.error(e);
                    }
                    return Mono.error(e); // Outros erros propagam para o retry
                })
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofMillis(retryBackoffMs))
                        .filter(this::isRetryable)
                        .doBeforeRetry(retrySignal -> log.warn("Tentativa de retry {} falhou. Retentando...", retrySignal.totalRetries() + 1))
                );
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException e) {
            // Retry apenas em 5xx (Server Error) ou 429 (Rate Limit)
            return e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
        }
        // Retry em erros de conexão (ex: timeout, network down)
        return true;
    }
}
