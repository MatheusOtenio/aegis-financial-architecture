***

# Aegis Financial Architecture — Backend Core







**Health Check:** `http://localhost:8080/actuator/health`  
**Metrics:** `http://localhost:8080/actuator/prometheus`

***

##  Sobre o Projeto

O **Aegis Backend Core** é o motor de processamento financeiro central da arquitetura Aegis. Ele foi projetado para ser um sistema de alta integridade, responsável por gerenciar o ciclo de vida de pedidos, processar pagamentos de forma atômica e consolidar relatórios financeiros diários.

Diferente de um CRUD tradicional, este backend isola completamente as regras de negócio da infraestrutura tecnológica, garantindo que mudanças em frameworks ou bancos de dados não afetem a lógica financeira (Domínio).

**Problema resolvido:** Eliminação de regras de negócio espalhadas (Front/BFF/Banco), garantia de consistência transacional em operações financeiras e rastreabilidade total (Auditoria) de alterações de estado.

***

##  Stack Tecnológica

### Core & Infraestrutura
- **Java 21** - Utilizando Records, Pattern Matching e Virtual Threads (preparado).
- **Spring Boot 3.4** - Framework base para Injeção de Dependência e Web.
- **PostgreSQL 16** - Banco de dados relacional robusto.
- **Docker & Compose** - Ambiente de desenvolvimento padronizado (Dev = Prod).

### Persistência & Qualidade
- **Spring Data JPA** - Abstração de acesso a dados.
- **Flyway** - Versionamento de Schema (Database as Code).
- **Testcontainers** - Testes de integração com banco de dados real (sem H2).
- **Logback + Jackson** - Logs estruturados em JSON para observabilidade.

***

##  Decisões Arquiteturais

### 1. Clean Architecture (Arquitetura Limpa)
O projeto foi estruturado em camadas concêntricas: `Domain` (Puro) → `Application` (Use Cases) → `Infrastructure` (Web/DB).
*   **Justificativa:** Protege o "coração" financeiro do software. Podemos trocar o banco de dados ou o framework web sem tocar em uma linha das regras de cálculo de preço ou transição de estados do pedido.

### 2. Flyway como Fonte da Verdade
Desabilitamos a geração automática de DDL do Hibernate (`ddl-auto: validate`) e delegamos tudo ao Flyway.
*   **Justificativa:** Em sistemas financeiros, não podemos correr o risco do ORM alterar colunas em produção inadvertidamente. O versionamento via scripts SQL garante reprodutibilidade exata do ambiente.

### 3. Service Token para API Interna
Para a comunicação entre o `Batch Job` e o `Backend`, implementamos uma autenticação via Header (`X-SERVICE-TOKEN`).
*   **Justificativa:** Simplicidade e performance. Para uma comunicação máquina-a-máquina dentro de uma rede privada (Docker Network), o OAuth2 seria *overengineering*. O Token fixo rotacionável resolve o requisito de segurança com latência zero.

### 4. Docker-First Development
O backend foi configurado para **não iniciar** sem o banco de dados via Docker.
*   **Justificativa:** Elimina o clássico "na minha máquina funciona". Garante que todo desenvolvedor (e o CI/CD) rode exatamente as mesmas versões de infraestrutura.

***

##  Problemas Enfrentados e Soluções

Durante o desenvolvimento, enfrentamos desafios técnicos que moldaram a solução final:

### Problema 1: Inconsistência entre Entidade Java e Banco
**Cenário:** O Hibernate tentava validar o schema antes do Flyway rodar as migrations, causando falha na inicialização (`Schema-validation: missing table`).
**Correção:** Ajuste na ordem de boot e configuração estrita no `docker-compose` (`depends_on: condition: service_healthy`), garantindo que a aplicação só suba quando o Postgres estiver pronto para aceitar as migrations.

### Problema 2: Tratamento de Erro de Segurança
**Cenário:** O endpoint protegido retornava `500 Internal Server Error` quando o token era inválido, poluindo os logs de erro.
**Correção:** Implementação de um `GlobalExceptionHandler` específico para capturar `ResponseStatusException`, traduzindo corretamente a exceção para HTTP `403 Forbidden` sem gerar stacktrace de erro no servidor.

### Problema 3: Testes Falsos Positivos com H2
**Cenário:** Bancos em memória (H2) muitas vezes aceitam sintaxes SQL que o Postgres rejeita (ex: JSONB).
**Correção:** Adoção do **Testcontainers**. Cada teste de integração sobe um container Postgres real, aplica as migrations do Flyway e executa o teste. Se passa no teste, passa em produção.

***

##  Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados.
- (Opcional) Java 21 para abrir o projeto na IDE.

### Passo a Passo

1. **Configurar Ambiente**
   ```bash
   cp .env.example .env
   ```

2. **Subir a Aplicação**
   ```bash
   # Na raiz do projeto (onde está o pom.xml)
   docker compose up --build -d
   ```

3. **Verificar Saúde**
   Acesse: `http://localhost:8080/actuator/health`

   *Saída esperada:*
   ```json
   {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}
   ```

***

##  Endpoints Principais

### API Pública (Cliente / BFF)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/orders` | Cria um novo pedido (Status: CREATED) |
| `POST` | `/orders/{id}/pay` | Processa pagamento (CREATED → PAID) |
| `POST` | `/orders/{id}/cancel` | Cancela pedido (CREATED → CANCELED) |
| `GET` | `/orders?date=YYYY-MM-DD` | Lista pedidos do dia |
| `GET` | `/reports/{date}` | Consulta relatório consolidado |

### API Interna (Batch / Sistema)

Requer Header: `X-SERVICE-TOKEN: super-secret-batch-token`

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/internal/reports/daily` | Registra/Consolida relatório diário |

***

##  Testes

O projeto possui uma suíte de testes de integração robusta.

```bash
# Executa testes unitários e de integração (sobe containers automaticamente)
mvn test
```

**Cenário Coberto (E2E):**
1. Sobe Postgres (Testcontainer).
2. Aplica Migrations (Flyway).
3. Cria Pedido via API (`POST /orders`).
4. Valida persistência e cálculo de total.
5. Executa Pagamento (`POST /pay`).
6. Valida mudança de status e auditoria.

***

##  Estrutura do Projeto

```text
src/main/java/com/aegis/backend
├── application      # Casos de Uso (Orquestração)
│   ├── audit
│   ├── order
│   └── report
├── domain           # Regras de Negócio (Puro Java)
│   ├── order        # Entidades: Order, OrderItem
│   └── report       # Entidade: DailyReport
├── infrastructure   # Detalhes Técnicos (Frameworks)
│   ├── persistence  # Repositórios JPA
│   └── web          # Controllers REST
└── shared           # Configurações globais
    ├── audit        # Entidade de Log de Auditoria
    └── exception    # Tratamento de Erros
```

***

##  Melhorias Futuras
*   Implementar autenticação OAuth2/OIDC (Keycloak) para substituir o Service Token se o sistema crescer.
*   Adicionar Cache (Redis) na leitura de relatórios (`GET /reports`).
*   Configurar pipeline de CI/CD (GitHub Actions) rodando o `mvn test`.

***

Desenvolvido com foco em **Robustez e Manutenibilidade**.