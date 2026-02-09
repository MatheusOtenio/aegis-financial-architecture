***

# Aegis Financial Architecture — Frontend (Console Operacional)

**URL padrão (dev):** `http://localhost:3001`  
**Dependência direta:** `Aegis BFF` em `http://localhost:3000`

***

## Sobre o Projeto

O **Aegis Frontend** é o console operacional da arquitetura Aegis. Ele foi desenhado para que o operador acompanhe, em uma única tela:

1. **Status do sistema**: saúde do BFF, Redis e (quando disponível) banco de dados.
2. **Criação de pedidos**: formulário guiado para envio de pedidos ao BFF.
3. **Dashboard diário**: listagem e resumo de pedidos por data.

Ao contrário de um frontend “genérico de CRUD”, este projeto é focado em:

- Expor **apenas os fluxos operacionais críticos**.
- Encapsular a complexidade de erros vindos do BFF/Backend em uma camada de **normalização + feedback visual**.
- Servir como **laboratório de UI** para validar contratos e jornadas antes de investir em telas definitivas.

**Problema resolvido:** Dar visibilidade operacional rápida sobre BFF/Backend, permitir criação de pedidos ponta a ponta e expor erros de forma amigável, sem que o operador precise conhecer a topologia completa (Redis, Backend Core, etc.).

***

## Stack Tecnológica

### Core & Framework

- **Next.js 16 (App Router)** – Estrutura de páginas, bundling e otimizações para React.
-,**React 19** – Base da camada de UI.
- **TypeScript 5** – Tipagem estrita para contratos e hooks.
- **React Query (TanStack Query v5)** – Camada de cache e orquestração de chamadas HTTP.

### Formulários & Validação

- **React Hook Form** – Controle de formulários performático e declarativo.
- **Zod 4** – Schemas de validação reutilizados entre UI e camada de API (`contracts`).
- **@hookform/resolvers** – Integração direta entre Zod e React Hook Form.

### Estilo & Design System

- **Tailwind CSS 4** – Utilizado de forma utilitária para layout e tipografia.
- **class-variance-authority / tailwind-merge / clsx** – Composição de classes utilitárias.
- **Radix UI (Dialog, Tooltip, Slot)** – Building blocks acessíveis para componentes interativos.
- **Geist / Geist Mono** – Fontes padrão carregadas via `next/font`.

### Infra & Observabilidade de UI

- **React Query Devtools** – Habilitado apenas em desenvolvimento.
- **Toast global customizado** – Feedback visual para erros não-relacionados a validação.
- **Camada de normalização de erros** – Converte erros HTTP em estruturas de alto nível (`AppError`).

***

## Decisões Arquiteturais

### 1. App Router + Providers Globais

- **Escolha:** Usar App Router (`src/app`) com `RootLayout` centralizando providers.
- **Implementação:**  
  - [layout.tsx](./src/app/layout.tsx) injeta:
    - `ToastProvider` – contexto de notificação global.
    - `ReactQueryProvider` – singleton do `QueryClient` + integração de erros.
- **Motivação:**  
  - Facilita a inserção de novas camadas transversais (tema, auth, etc.).
  - Garante que qualquer página/feature tenha acesso a cache e toasts sem wiring manual.

### 2. React Query como “cola” entre UI e BFF

- **Escolha:** Centralizar chamadas HTTP em hooks baseados em React Query (`useQuery`, `useMutation`).
- **Positivos:**
  - Cache automático e invalidação simples (ex.: após criar pedido, invalidar `["orders"]`).
  - Tratamento de estado “loading / error / success” padronizado.
- **Negativos:**
  - Introduz uma camada de complexidade (query keys, cache) que precisa ser bem documentada.
  - Depende fortemente de **disciplinar** o uso de keys e estratégias de retry.

### 3. Normalização de Erros + Toast Global

- **Escolha:** Toda chamada ao BFF é feita via [`apiFetch`](./src/lib/api/http-client.ts), que traduz o mundo HTTP em um tipo unificado `AppError`.  
- **Camadas:**
  1. `http-client.ts` converte respostas HTTP em erros tipados (`validation`, `backend`, `rate-limit`, `internal`).
  2. `mapAppErrorToMessage.ts` transforma esses erros em mensagens legíveis para UI.
  3. `useQueryErrorToast` decide quando exibir toasts (ignora erros de validação de formulário).
  4. `ReactQueryProvider` liga tudo via `QueryCache`/`MutationCache.onError`.
- **Positivos:**
  - Frontend mostra mensagens consistentes independente de onde o erro surgiu (BFF, rede, regra de negócio).
  - Erros de validação não “poluem” o usuário com toasts (são renderizados ao lado dos campos).
- **Negativos:**
  - Requer disciplina para que **todas** as chamadas usem `apiFetch`.
  - Mapeamento precisa ser mantido em sincronia com contratos do BFF.

### 4. UI como “cola”, não como lugar de regra de negócio

- **Escolha:** Containers de feature (Orders, System Status) focam em:
  - Chamar hooks de dados.
  - Compor layout e exibir estado.
- **Exemplos:**
  - [`SystemStatusContainer`](./src/features/system-status/containers/SystemStatusContainer.tsx) apenas consome `useSystemHealth` e renderiza badges/links.
  - [`CreateOrderContainer`](./src/features/orders/containers/CreateOrderContainer.tsx) orquestra formulário, mas delega persistência para `useCreateOrder`.
- **Motivação:**  
  - Facilitar refactors quando contratos mudarem no BFF.
  - Permitir reuso dos hooks em outras telas no futuro.

***

## Como Executar

### Pré-requisitos

- Node.js 20+ instalado.
- Docker Desktop (recomendado) se você for subir BFF e Backend juntos.

### Passo a Passo (Somente Frontend)

```bash
cd aegis-frontend
npm install

# Dev server
npm run dev
```

- Acesse: `http://localhost:3000` (porta padrão do Next em dev).
- Certifique-se de que `NEXT_PUBLIC_BFF_BASE_URL` aponta para o BFF (`http://localhost:3000` ou equivalente).

### Passo a Passo (Frontend + BFF via Docker)

1. Subir o BFF (porta 3000):

   ```powershell
   cd ..\aegis-bff
   docker compose up -d
   ```

2. Buildar e subir o frontend (porta 3001):

   ```powershell
   cd ..\aegis-frontend
   docker build -t aegis-frontend `
     --build-arg NEXT_PUBLIC_BFF_BASE_URL=http://localhost:3000 `
     --build-arg NEXT_PUBLIC_USE_MOCKS_UI=true `
     .

   docker run --rm -p 3001:3001 --name aegis-frontend aegis-frontend
   ```

3. Acessar o console operacional:

- Frontend: `http://localhost:3001`
- BFF (Swagger, métricas): `http://localhost:3000/docs`, `http://localhost:3000/metrics`

***

## Variáveis de Ambiente

As variáveis públicas são obrigatórias para a aplicação funcionar. Em desenvolvimento, use `.env.local` na raiz do projeto.

```env
NEXT_PUBLIC_BFF_BASE_URL=http://localhost:3000
NEXT_PUBLIC_USE_MOCKS_UI=true
```

- `NEXT_PUBLIC_BFF_BASE_URL`  
  - Aponta para o BFF.  
  - Em Docker, pode continuar sendo `http://localhost:3000`, já que quem consome é o navegador.
- `NEXT_PUBLIC_USE_MOCKS_UI`  
  - Controla apenas o **badge de mocks** exibido na UI.  
  - Idealmente deve refletir o valor de `USE_MOCKS` no BFF.

> Importante: o `http-client.ts` lança erro se `NEXT_PUBLIC_BFF_BASE_URL` não estiver definido, para evitar ambientes parcialmente configurados.

***

## Fluxo Principal (Pedidos)

1. Usuário preenche o formulário em `CreateOrderContainer` (cliente + itens).
2. Hook `useCreateOrder` chama `postCreateOrder` → `apiFetch("/api/orders")`.
3. BFF:
   - Valida payload com Zod.
   - Em `USE_MOCKS=true`, simula criação de pedido.
   - Em `USE_MOCKS=false`, delega para Backend Core.
4. Frontend:
   - Em caso de sucesso:
     - Mostra alerta básico (ID + status).
     - Invalida queries de listagem de pedidos.
   - Em caso de erro:
     - Erros de validação vão para o formulário (field errors).
     - Outros erros disparam toast global.

***

## Observabilidade na UI

### System Status

- Tela inicial mostra:
  - Status geral do BFF (`READY` ou `NOT_READY`) baseado em `/health/ready`.
  - Status de Redis e Database (se expostos pelo BFF).
- Ações:
  - Botão de “Tentar novamente”.
  - Links rápidos:
    - Swagger: `http://localhost:3000/docs`
    - Métricas Prometheus do BFF: `http://localhost:3000/metrics`

### Toasts Globais

- Tratam erros de rede, 5xx, rate limit, etc.
- Intenção é ser **informativo**, não intrusivo:
  - Título genérico (“Erro ao processar solicitação”).
  - Mensagem detalhada vinda de `mapAppErrorToMessage`.

***

## Estrutura do Projeto (Resumo)

```text
src/
├── app
│   ├── layout.tsx           # Root layout com providers globais
│   ├── page.tsx             # Console operacional (status + pedidos)
│   ├── react-query-provider.tsx
│   └── toast-provider.tsx
├── contracts                # DTOs e schemas Zod compartilhados
├── features
│   ├── orders               # Containers + hooks de pedidos
│   └── system-status        # Container + hooks de health
└── lib
    ├── api                  # http-client + wrappers de endpoints
    ├── errors               # Normalização de erros
    └── query                # Funções auxiliares de React Query
```

***

## Limitações Atuais

### Catálogo de Clientes/Produtos

- Formulário exige `customerId` e `productId` como UUID, mas:
  - Não existe hoje catálogo de clientes/produtos no Backend.
  - A UI não tem autocomplete nem busca; o operador precisa informar UUIDs manualmente.
- Situação atual:
  - Qualquer UUID válido é aceito.
  - Backend Core ignora esses identificadores (não há colunas/tabelas para eles).

### UX e Acessibilidade

- Layout focado em fluxo operacional, não em UX final.
- Acessibilidade básica, mas ainda sem testes dedicados (`aria-*`, navegação por teclado, etc.).

### Testes

- Cobertura de testes focada no BFF e Backend.
- Frontend ainda não possui:
  - Testes de componentes.
  - Testes de integração de hooks com React Query.

***

## Melhorias Futuras

- Implementar catálogo de Clientes/Produtos no Backend + UI de seleção (autocomplete).
- Evoluir dashboard de pedidos com:
  - Paginação.
  - Filtros por status.
  - Indicadores de resumo (total por status).
- Adicionar testes de UI (unitários e de integração).
- Integrar autenticação real quando a arquitetura de segurança estiver definida.
