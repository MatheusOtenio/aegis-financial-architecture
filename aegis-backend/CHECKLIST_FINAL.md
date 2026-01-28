# Checklist de Entrega — Aegis Backend

## Requisitos Funcionais
- [x] CRUD de Pedidos (Criar, Pagar, Cancelar, Listar).
- [x] Regras de Domínio (Não pagar pedido cancelado, não itens negativos).
- [x] Relatórios Diários (API de leitura e escrita interna).
- [x] Auditoria (Tabela `audit_logs` funcional).

## Requisitos Não-Funcionais
- [x] Java 21 + Spring Boot 3.
- [x] Docker-first (Backend não roda sem container de banco).
- [x] Flyway como fonte de verdade do Schema.
- [x] Logs em JSON.
- [x] Health Check detalhado.

## Qualidade e Testes
- [x] Testes de Integração com Testcontainers.
- [x] Tratamento de Exceções Global (4xx/5xx corretos).
```
