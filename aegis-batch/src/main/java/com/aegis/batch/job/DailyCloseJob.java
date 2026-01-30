package com.aegis.batch.job;

import com.aegis.batch.application.DailyReportCalculator;
import com.aegis.batch.infrastructure.client.BackendClient;
import com.aegis.batch.shared.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyCloseJob {

    private final BackendClient backendClient;
    private final DailyReportCalculator calculator;

    // Cron expression lida do application.yml.
    // Zone define o fuso horário crítico para o "fechamento do dia" correto.
    @Scheduled(cron = "${aegis.job.cron}", zone = "America/Sao_Paulo")
    public void executeDailyClose() {
        // 1. Setup de Contexto e Correlação
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        // 2. Definir Data Alvo (Ontem, D-1)
        // Importante: LocalDate.now() deve respeitar o timezone do sistema/container
        LocalDate targetDate = LocalDate.now().minusDays(1);
        MDC.put("reportDate", targetDate.toString());

        log.info("INICIANDO JOB DE FECHAMENTO. Data Alvo: {}", targetDate);

        try {
            // 3. Orquestração Reativa (Bloqueante no final pois Scheduled é void)
            // Lógica: Stream Orders -> Calculate -> Post Report
            Flux<OrderDto> orderStream = backendClient.streamOrdersByDate(targetDate);

            calculator.calculate(targetDate, orderStream)
                    .flatMap(report -> {
                        log.info("Relatório calculado: Orders={}, Revenue={}", report.totalOrders(), report.totalRevenue());
                        return backendClient.postDailyReport(report);
                    })
                    .doOnSuccess(v -> log.info("JOB FINALIZADO COM SUCESSO."))
                    .doOnError(e -> log.error("JOB FALHOU FATALMENTE.", e))
                    .block(); // Bloqueia thread do scheduler até terminar o fluxo reativo

        } catch (Exception e) {
            log.error("Exceção não tratada na execução do job", e);
        } finally {
            MDC.clear(); // Limpeza vital para não poluir logs de outras execuções
        }
    }
}
