import { Router } from 'express';
import { ordersController } from './controllers/ordersController';
import { healthController } from './controllers/healthController';
import { validate } from './middleware/validate';
import { CreateOrderSchema } from './dtos/order.dto';

const router = Router();

// Observabilidade
router.get('/health', healthController.liveness.bind(healthController)); // Legacy/Simples
router.get('/health/live', healthController.liveness.bind(healthController));
router.get('/health/ready', healthController.readiness.bind(healthController));
router.get('/metrics', healthController.metrics.bind(healthController));

// Negócio
router.post('/api/orders', validate(CreateOrderSchema), ordersController.create.bind(ordersController));
router.get('/api/orders', ordersController.list.bind(ordersController));

export { router };
