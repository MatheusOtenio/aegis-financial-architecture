
***

# Aegis Financial Architecture — Batch Service

**Health Check:** `http://localhost:8081/actuator/health`
**Metrics:** `http://localhost:8081/actuator/metrics`

***

## Sobre o Projeto

O **Aegis Batch Service** é o componente responsável pelo fechamento financeiro diário da plataforma Aegis. Ele orquestra a consolidação dos dados operacionais, garantindo que o volume financeiro processado ao longo do dia seja sumarizado e auditado de forma imutável.

Projetado para ser leve e independente, este serviço roda como um processo efêmero (ou daemon agendado) que se conecta ao Backend principal apenas via API, seguindo o padrão API RESTful, sem acesso direto ao banco de dados, garantindo desacoplamento total e segurança reforçada.

**Problema resolvido:** Automatização do fechamento diário, eliminação de processos manuais de contabilidade e isolamento de carga pesada de leitura/escrita em horários de menor tráfego (D-1).

***

## Stack Tecnológica

### Core & Infraestrutura
- **Java 21** - Utilizando `Record` para DTOs imutáveis e Virtual Threads (via WebFlux).
- **Spring Boot 3.4** - Framework base com foco em jobs e tarefas agendadas.
- **Spring WebFlux (WebClient)** - Cliente HTTP reativo para consumo de streams de dados sem estourar memória (Backpressure).
- **Docker** - Containerização completa para garantir execução consistente do Timezone e dependências.

### Observabilidade & Resiliência
- **Resilience4j / Reactor Retry** - Políticas de retentativa exponencial para falhas transitórias de rede.
- **Micrometer & Actuator** - Exposição de métricas de saúde e performance.
- **Logback (MDC)** - Rastreabilidade de execução via `correlationId`.

***

## Decisões Arquiteturais

### 1. API-Only Architecture (Sem Banco de Dados)
O Batch não se conecta ao banco de dados. Ele consome dados via **Streaming JSON** do Backend.
*   **Justificativa:** Acesso direto ao banco de outro microserviço é um anti-pattern (Shared Database) que gera acoplamento rígido. Ao usar a API, respeitamos as regras de negócio e a segurança impostas pelo Backend Core.

### 2. Streaming Reativo (Flux)
Em vez de baixar uma lista gigante de pedidos (`List<Order>`) na memória, processamos o JSON item a item (`Flux<OrderDto>`).
*   **Justificativa:** Prevenção de `OutOfMemoryError` (OOM). Mesmo que o dia tenha 1 milhão de pedidos, o Batch consome memória constante e baixa, pois descarta o objeto assim que ele é somado ao agregador.

### 3. Idempotência Semântica
O Batch trata o erro HTTP `400 Bad Request` (Report Already Exists) como **Sucesso**.
*   **Justificativa:** Se o job rodar duas vezes (acidentalmente), a segunda execução não deve falhar o pipeline nem duplicar dados. O Backend rejeita a duplicação, e o Batch entende isso como "trabalho já feito".

### 4. Segurança via Service Token
Autenticação via Header `X-SERVICE-TOKEN` injetado via variável de ambiente.
*   **Justificativa:** Simples, rotacionável e eficiente para comunicação interna Server-to-Server dentro da malha segura do Docker.

***

## Guia de Configuração

As variáveis abaixo são obrigatórias para a execução do container.

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `BACKEND_BASE_URL` | URL base do Aegis Backend Core | `http://host.docker.internal:8080` |
| `X_SERVICE_TOKEN` | Token secreto compartilhado com o Backend | `super-secret-batch-token` |
| `AEGIS_BATCH_MAX_RETRIES` | Máximo de tentativas em caso de erro 5xx | `5` (Default: 3) |
| `AEGIS_BATCH_RETRY_BACKOFF_MS` | Tempo de espera inicial entre retries | `1000` (Default: 500ms) |
| `TZ` | Timezone do Container (Crucial para D-1) | `America/Sao_Paulo` |

***

## Diário de Bordo: Desafios e Soluções

Durante a integração final, superamos desafios críticos de segurança e comunicação. Este registro serve como base de conhecimento para manutenção futura.

### 1. Mismatch de Autenticação (Erro 403)
**Problema:** O Batch recebia `403 Forbidden` ao tentar ler os pedidos (`GET /internal/orders/daily`), mesmo acreditando estar autenticado.
*   **Causa Raiz:** Divergência de configuração. O Batch enviava o header baseado na chave `aegis.backend.token`, mas o Backend esperava validação manual contra a propriedade `aegis.security.service-token`.
*   **Diagnóstico:** Análise dos logs mostrou `WebClientResponseException$Forbidden`. Revisão do código do Backend confirmou que não havia Spring Security, mas uma validação manual (`if (!token.equals(expected))`) no Controller.
*   **Resolução:** Unificação das chaves. O ambiente Docker foi ajustado para injetar o mesmo valor (`super-secret-batch-token`) em ambos os serviços via `AEGIS_SERVICE_TOKEN`.

### 2. Confusão entre Segurança Spring vs Validação Manual
**Problema:** Hipótese inicial de que o bloqueio do GET era causado por filtros do Spring Security (`requestMatchers`).
*   **Causa Raiz:** Suposição baseada em padrões comuns de mercado, ignorando a implementação específica "leve" do Backend atual.
*   **Diagnóstico:** Verificação do `pom.xml` do Backend revelou ausência do `spring-boot-starter-security`. Isso eliminou a hipótese de filtros de cadeia e redirecionou o foco para a lógica do Controller.
*   **Resolução:** Ajuste mental do modelo de segurança. O problema não era configuração de infraestrutura, mas sim dados (token incorreto). Correção aplicada nas variáveis de ambiente.

### 3. Inexistência do Endpoint Manual
**Problema:** Tentativas de disparar o job manualmente via `POST /manual/jobs/daily-close` resultavam em 404 ou conexão recusada.
*   **Causa Raiz:** O endpoint não existia. O Job era apenas uma tarefa agendada (`@Scheduled`) sem gatilho HTTP exposto.
*   **Resolução:** Criação do `ManualTriggerController` na camada de infraestrutura web. Este endpoint foi desenhado especificamente para operação e debug, disparando o job em uma thread separada para não bloquear a resposta HTTP.

### 4. Validação de Segurança no Job Manual (Erro 401)
**Problema:** Ao chamar o novo endpoint manual, recebemos `401 Unauthorized`.
*   **Causa Raiz:** O novo controller manual também exige o header `X-SERVICE-TOKEN` por segurança, mas o teste (curl) foi feito sem ele.
*   **Resolução:** Inclusão do header nas chamadas manuais de operação.
    ```bash
    curl -X POST http://localhost:8080/manual/jobs/daily-close \
      -H "X-SERVICE-TOKEN: super-secret-batch-token"
    ```

### 5. Tratamento de Idempotência (Erro 400)
**Problema:** O Batch rodava corretamente, calculava os valores, mas o passo final de envio falhava com `400 Bad Request`.
*   **Causa Raiz:** O Backend já possuía um relatório fechado para aquela data (criado em teste anterior). O retorno 400 é a forma do Backend dizer "Já existe, não vou criar outro".
*   **Resolução:** Implementação de tratamento de erro no `BackendClient`. O status 400 agora é capturado e logado como "Sucesso Semântico" (Idempotência), permitindo que o Job finalize com status VERDE (Sucesso) em vez de VERMELHO (Falha).

***

## Como Executar

### Via Docker (Produção / Homologação)

```bash
# 1. Build da Imagem
docker build -t aegis-batch:latest .

# 2. Execução (Conectando ao Backend no Host)
docker run -d --name aegis-batch \
  -p 8081:8081 \
  -e BACKEND_BASE_URL="http://host.docker.internal:8080" \
  -e X_SERVICE_TOKEN="super-secret-batch-token" \
  -e TZ="America/Sao_Paulo" \
  aegis-batch:latest
```

### Gatilho Manual

Para forçar o processamento imediato (sem esperar o agendamento da madrugada):

```bash
curl -X POST http://localhost:8080/manual/jobs/daily-close \
  -H "X-SERVICE-TOKEN: super-secret-batch-token"
```

***

## Endpoints Disponíveis

Este serviço não expõe API de negócio, apenas endpoints operacionais.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/actuator/health` | Status da aplicação (Liveness/Readiness) |
| `POST` | `/manual/jobs/daily-close` | **Operacional:** Força a execução imediata do fechamento (D-1) |

***

##  Qualidade e Observabilidade

Este projeto adota práticas de engenharia de software de nível corporativo (*Production Grade*), garantindo que o código seja testável, monitorável e rastreável.

####  Estratégia de Testes
Seguimos a pirâmide de testes para garantir confiança sem sacrificar a velocidade de build.

*   **Testes de Unidade (`JUnit 5` + `Mockito`):**
    *   Validam a lógica de negócio isolada na `DailyReportCalculator`.
    *   Garantem que a matemática financeira (soma de `BigDecimal`) e as regras de filtragem (apenas status `PAID`) estejam corretas independente de infraestrutura.
*   **Testes de Integração (`WireMock` + `Reactor Test`):**
    *   O `BackendClientTest` sobe um servidor HTTP simulado (WireMock) para validar a comunicação real via rede.
    *   Testamos cenários de "Caminho Feliz" (200 OK), "Idempotência" (400 Bad Request) e "Resiliência" (Retries em 500 Server Error).
*   **Cobertura Reativa:**
    *   Utilizamos `StepVerifier` para testar os fluxos reativos (`Flux/Mono`), garantindo que eventos de erro e completude sejam emitidos corretamente.

Para rodar a suíte completa:
```bash
mvn test
```

##  Observabilidade e Logs
A aplicação foi desenhada para ser "transparente" em operação, permitindo diagnóstico rápido sem acesso ao código fonte.

*   **Rastreamento Distribuído (Correlation ID):**
    *   Cada execução do Job gera um `correlationId` único (UUID).
    *   Este ID é injetado automaticamente em todos os logs via **MDC (Mapped Diagnostic Context)**.
    *   *Benefício:* Permite filtrar todos os logs de uma execução específica do batch em ferramentas como Kibana/Splunk, mesmo que requests HTTP se misturem.
*   **Logs Estruturados:**
    *   Os logs não mostram apenas "Erro", mas o contexto: `[CorrId: a1b2...] Relatório calculado: Orders=150, Revenue=15000.00`.
*   **Métricas (Actuator):**
    *   Exposição nativa de métricas da JVM, uso de CPU e estado do pool de threads em `/actuator/metrics`.

***


Desenvolvido com foco em **Eficiência e Imutabilidade**.