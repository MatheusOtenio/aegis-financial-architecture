import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';

// Configuração de CORS: Permite apenas o Frontend específico (ou * em dev)
export const corsConfig = cors({
    origin: process.env.CORS_ORIGIN || '*', // Em prod, setar para o domínio do frontend
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Correlation-Id'],
});

// Configuração do Helmet: Headers de segurança padrão
export const helmetConfig = helmet();

// Rate Limiting: 100 requisições por 15 minutos por IP
export const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutos
    max: 100,
    standardHeaders: true, // Retorna info de rate limit nos headers `RateLimit-*`
    legacyHeaders: false, // Desabilita headers `X-RateLimit-*`
    message: { error: 'Too many requests, please try again later.' }
});
