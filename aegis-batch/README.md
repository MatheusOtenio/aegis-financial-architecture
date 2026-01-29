# Aegis Batch

Serviço de fechamento diário responsável por calcular relatórios e enviá-los ao backend.

## Variáveis de Ambiente Obrigatórias

| Variável | Descrição | Exemplo |
|---|---|---|
| `DATABASE_URL` | URL JDBC de leitura do Postgres | `jdbc:postgresql://localhost:5432/db` |
| `DATABASE_USER` | Usuário do banco (apenas leitura) | `readonly_user` |
| `DATABASE_PASSWORD` | Senha do banco | `secret` |
| `BACKEND_BASE_URL` | URL base do serviço Backend | `http://backend:8080` |
| `X_SERVICE_TOKEN` | Token estático para autenticação | `super-secret-batch-token` |

### Opcionais (Defaults)
*   `AEGIS_BATCH_MAX_RETRIES`: Número máximo de tentativas (Default: `3`)
*   `AEGIS_BATCH_RETRY_BACKOFF_MS`: Backoff inicial em ms (Default: `500`)

## Contrato de Interface (Backend)

O batch envia os relatórios para o seguinte endpoint:

**POST** `/internal/reports/daily`

*   **201 Created**: Sucesso (Novo relatório).
*   **400 Bad Request**: Sucesso (Idempotência - Relatório já existe).
*   **403 Forbidden**: Erro Fatal (Token inválido).
*   **5xx**: Erro Transitório (Requer Retry).
