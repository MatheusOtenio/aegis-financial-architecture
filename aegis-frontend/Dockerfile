FROM node:20-alpine AS builder

WORKDIR /app

ENV NEXT_TELEMETRY_DISABLED=1

ARG NEXT_PUBLIC_BFF_BASE_URL
ARG NEXT_PUBLIC_USE_MOCKS_UI
ENV NEXT_PUBLIC_BFF_BASE_URL=${NEXT_PUBLIC_BFF_BASE_URL}
ENV NEXT_PUBLIC_USE_MOCKS_UI=${NEXT_PUBLIC_USE_MOCKS_UI}

COPY package.json package-lock.json* ./

RUN npm ci --include=dev

COPY . .

RUN npm run build


FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3001

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY package.json package-lock.json* ./

RUN npm ci --omit=dev

EXPOSE 3001

CMD ["npm", "run", "start"]
