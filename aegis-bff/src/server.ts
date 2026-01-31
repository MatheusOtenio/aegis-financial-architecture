import express, { Request, Response } from 'express';
import dotenv from 'dotenv';
import { router } from './routes';
import { errorHandler } from './middleware/errorHandler';
import { helmetConfig, corsConfig, limiter } from './middleware/security';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// ----------------------
// Middlewares de Segurança
// ----------------------
// Devem vir antes de qualquer rota
app.use(helmetConfig);
app.use(corsConfig);
app.use(limiter);

// ----------------------
// Middleware de parsing
// ----------------------
app.use(express.json());

// ----------------------
// Rota Health
// ----------------------
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ----------------------
// Rotas da API
// ----------------------
app.use(router);

// ----------------------
// Tratamento de erros (DEVE ser o último middleware)
// ----------------------
app.use(errorHandler);

// ----------------------
// Inicialização do servidor
// ----------------------
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`BFF running on port ${PORT}`);
    console.log(`Mocks enabled: ${process.env.USE_MOCKS === 'true'}`);
  });
}

export default app;
