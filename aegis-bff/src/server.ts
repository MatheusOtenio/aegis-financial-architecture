import express, { Request, Response } from 'express';
import dotenv from 'dotenv';
import { router } from './routes';
import { errorHandler } from './middleware/errorHandler';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Rota Health
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Rotas da API
app.use(router);

// Tratamento de erros (DEVE ser o último middleware)
app.use(errorHandler);

if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`BFF running on port ${PORT}`);
    console.log(`Mocks enabled: ${process.env.USE_MOCKS === 'true'}`);
  });
}

export default app;
