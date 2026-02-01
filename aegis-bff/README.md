# Aegis Financial Architecture — BFF (Backend-for-Frontend)

**Health Check:** `http://localhost:3000/health/ready`  
**Swagger UI:** `http://localhost:3000/docs`  
**Porta padrão:** `http://localhost:3000`

***

## Sobre o Projeto

O **Aegis BFF** é uma camada de adaptação entre o Frontend (ainda em desenvolvimento) e o **Backend Core** (Java/Spring). Sua existência resolve três problemas concretos:

1. **Agregação de dados**: O frontend não precisa fazer 3 chamadas HTTP separadas para compor uma tela
2. **DTOs UI-friendly**: Transforma `snake_case` do backend em `camelCase` e adiciona campos de apresentação (`displayStatus`)
3. **Isolamento**: Mudanças no Backend Core (ex: campo `audit.created_by` removido) não quebram o frontend

**Status atual**: Contratos mínimos, desenvolvido **antes** do frontend existir. DTOs são provisórios e evolutivos.

***

## Stack Tecnológica

### Core & Framework
- **Node.js v24 (Active LTS)** - Versão estável para produção
- **Express v5** - API moderna, compatível com TypeScript
- **TypeScript 5.3+** - Tipagem estrita em tempo de compilação
- **Zod 3.x** - Validação de DTOs em runtime (source of truth para contratos)

### Infraestrutura & Dados
- **Docker Compose** - Redis local para cache (desenvolvimento = produção)
- **Redis** - Cache de leitura TTL=10s (listagens de pedidos)
- **Axios** - Cliente HTTP para Backend Core (`localhost:8080`)

### Qualidade & Observabilidade
- **Jest + Supertest** - Testes unitários + integração HTTP
- **ESLint + Prettier** - Formatação consistente
- **Prom-client** - Métricas de sistema (CPU/Memória)
- **Helmet + RateLimit** - Segurança básica HTTP

***

## Decisões Arquiteturais

### 1. Express v5 + TypeScript (vs Fastify/NestJS)
```
Escolha A: Express v5 → Simplicidade, ecossistema maduro, curva de aprendizado zero
Escolha B: NestJS → Enterprise-ready, mas 3x mais boilerplate para um BFF simples
```
**Decidido**: Express. BFF não precisa de DI container ou decorators complexos.

### 2. Separação Estrita: Controller → Service → DTO
```
Frontend DTO (Zod) → Controller (valida) → Service (orquestra) → Backend DTO → Backend Core
```
**Justificativa**: Cada camada tem responsabilidade única. Controller é "fininho", Service contém retry/cache logic.

### 3. DTOs Mínimos (id, status, totalAmount)
**Realidade**: Frontend não existe. Implementamos apenas o mínimo viável para validar fluxo E2E.
**Risco**: Campos adicionais (customer name, items details) precisarão ser adicionados quando UI evoluir.

### 4. Mocks com Toggle (USE_MOCKS=true)
**Estratégia**: Backend Core acessível (`localhost:8080`), mas desenvolvedor BFF não bloqueia por dependência externa.
```
env.USE_MOCKS=true  → Retorna dados fake instantâneos
env.USE_MOCKS=false → Chama Backend Core real via Axios
```

***

## Como Executar

### Pré-requisitos
```
- Node.js v24 (verifique: node -v)
- Docker Desktop (Linux containers)
```

### Passo a Passo

```powershell
#  Dentro da pasta aegis-bff
npm install

#  Redis para cache (Docker)
docker compose up -d redis

#  Desenvolvimento (hot reload)
npm run dev
```

**Verificação:**
```powershell
# Health completo (testa Redis connectivity)
curl.exe http://localhost:3000/health/ready

# Swagger UI (teste interativo)
# Navegador: http://localhost:3000/docs
```

### Build de Produção
```powershell
npm run build    # tsc → pasta dist/
npm start        # node dist/server.js
```

***

## Variáveis de Ambiente

Crie `.env` na raiz:

```env
PORT=3000
BACKEND_URL=http://host.docker.internal:8080
REDIS_HOST=redis
REDIS_PORT=6379
USE_MOCKS=true                            # true=desenvolvimento, false=backend real
CORS_ORIGIN=*                             # Domínio do frontend em prod
X_SERVICE_TOKEN=super-secret-batch-token  # Para chamadas internas futuras
```

***

## Diário de Bordo — Desafios Técnicos

### Problema 1: DTOs sem Frontend Existente
**Cenário**: "Quais campos o frontend realmente precisa?"  
**Causa Raiz**: Contratos desenhados unilateralmente  
**Diagnóstico**: DTO mínimo viável (id, status, total)  
**Solução**: `displayStatus` em PT-BR como valor agregado  
**Trade-off**: Simplicidade agora vs Refatoração futura (inevitável)

### Problema 3: Redis Leak nos Testes Jest
**Cenário**: `npm test` travava (conexão ioredis aberta)  
**Causa Raiz**: Singleton Redis não desconectava no `afterAll`  
**Diagnóstico**: `--forceExit` no script de teste  
**Solução**: Aceitar trade-off (testes rápidos > desconexão perfeita)  
**Trade-off**: Processo Jest pode demorar 2s vs Testes confiáveis

### Problema 4: Express v5 TypeScript Conflicts
**Cenário**: Tipagens `@types/express` não 100% compatíveis  
**Decisão**: Manter v5 + ajustes manuais  
**Solução**: Usar `any` apenas onde necessário  
**Trade-off**: Pequenos `as any` vs Framework mais novo

***

## Endpoints Implementados

### API Pública (Frontend)

| Método | Endpoint | Descrição | Validações/Observações |
|--------|----------|-----------|----------------------|
| `POST` | `/api/orders` | Cria pedido | Zod: `customerId` UUID, `items[]` obrigatórios |
| `GET` | `/api/orders?date=YYYY-MM-DD` | Lista do dia (cache Redis 10s) | Default: hoje |

### Observabilidade

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/health/ready` | Liveness + Redis ping |
| `GET` | `/metrics` | Prometheus (sistema apenas) |
| `GET` | `/docs` | Swagger UI interativo |

**Exemplo Real (PowerShell):**
```powershell
curl.exe -X POST http://localhost:3000/api/orders -H "Content-Type: application/json" -d '{\"customerId\":\"123e4567-e89b-12d3-a456-426614174000\",\"items\":[{\"productId\":\"123e4567-e89b-12d3-a456-426614174001\",\"quantity\":1,\"unitPrice\":99.90}]}'
```

***

## Observabilidade Atual

### Health Checks
```
GET /health/ready → Verifica apenas Redis (PONG esperado)
Status: 200 READY | 503 NOT_READY
```

### Logs
```
Console.log simples (sem estrutura JSON ainda)
Futuro: correlationId, backendLatencyMs
```

### Métricas
```
GET /metrics → Apenas sistema (CPU, memória Node.js)
Métricas de negócio (pedidosCriadosTotal) adiadas
```

**Por que adiar?** Sem frontend fazendo chamadas reais, contadores seriam zeros.

***

## Limitações Atuais

###  Contratos Provisórios
```
DTOs: id, status, totalAmount, displayStatus
Faltam: customer details, item breakdown, paginação
```

###  Mocks Ativos
```
USE_MOCKS=true → Dados fake instantâneos
Backend real: http://localhost:8080 (testado, mas toggle=false)
```

###  Observabilidade
```
Sem: Correlation ID, distributed tracing, business metrics
Motivo: Prioridade foi fluxo E2E funcional
```

###  Testes
```
Cobertura: createOrder + listOrders + validação Zod
Faltam: payOrder, cancelOrder, contract tests
```


