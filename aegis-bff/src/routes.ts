import { Router } from 'express';
import { ordersController } from './controllers/ordersController';
import { validate } from './middleware/validate';
import { CreateOrderSchema } from './dtos/order.dto';

const router = Router();

// Rotas de Pedidos
router.post('/api/orders', validate(CreateOrderSchema), ordersController.create.bind(ordersController));
router.get('/api/orders', ordersController.list.bind(ordersController));

export { router };
