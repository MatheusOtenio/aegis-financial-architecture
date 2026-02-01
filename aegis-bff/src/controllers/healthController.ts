import { Request, Response } from 'express';
import { redis } from '../lib/redis';
import client from 'prom-client';

// Métricas padrão (CPU, Memória, etc)
const collectDefaultMetrics = client.collectDefaultMetrics;
collectDefaultMetrics();

export class HealthController {
  
  // Liveness: O processo está rodando?
  async liveness(req: Request, res: Response) {
    res.status(200).json({ status: 'UP' });
  }

  // Readiness: Dependências essenciais (Redis) estão ok?
  async readiness(req: Request, res: Response) {
    try {
      const redisStatus = await redis.ping(); // Retorna 'PONG' se ok
      if (redisStatus !== 'PONG') throw new Error('Redis ping failed');

      res.status(200).json({ 
        status: 'READY',
        checks: {
          redis: 'UP',
          database: 'SKIPPED' // Backend core check poderia vir aqui
        }
      });
    } catch (error) {
      res.status(503).json({ 
        status: 'NOT_READY',
        checks: {
          redis: 'DOWN',
          error: (error as Error).message
        }
      });
    }
  }

  // Metrics: Formato Prometheus
  async metrics(req: Request, res: Response) {
    try {
      res.set('Content-Type', client.register.contentType);
      const metrics = await client.register.metrics();
      res.send(metrics);
    } catch (error) {
      res.status(500).send(error);
    }
  }
}

export const healthController = new HealthController();
