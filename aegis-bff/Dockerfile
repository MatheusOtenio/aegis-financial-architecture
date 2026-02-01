# Stage 1: Builder
FROM node:24-alpine AS builder
WORKDIR /app

# Instalar dependências (incluindo devDependencies para compilar TS)
COPY package*.json ./
RUN npm ci

# Copiar código fonte e buildar
COPY . .
RUN npm run build

# Stage 2: Runtime
FROM node:24-alpine
WORKDIR /app

# Copiar apenas arquivos necessários para produção
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/openapi ./openapi

# Instalar apenas dependências de produção
RUN npm ci --omit=dev

# Variáveis de ambiente padrão (podem ser sobrescritas pelo docker-compose)
ENV PORT=3000
ENV NODE_ENV=production

EXPOSE 3000

CMD ["node", "dist/server.js"]
