import { Request, Response, NextFunction } from 'express';
import { orderService } from '../services/orderService';
import { CreateOrderInput } from '../dtos/order.dto';

export class OrdersController {
    async create(req: Request, res: Response, next: NextFunction) {
        try {
            // O body já foi validado pelo middleware, mas forçamos a tipagem aqui
            const input: CreateOrderInput = req.body;
            const order = await orderService.createOrder(input);
            res.status(201).json(order);
        } catch (error) {
            next(error);
        }
    }

    // Placeholder para GET /orders (implementaremos lógica real na próxima fase)
    async list(req: Request, res: Response, next: NextFunction) {
        try {
            // Retorna array vazio por enquanto apenas para validar rota
            res.json({ data: [], meta: { page: 1, total: 0 } });
        } catch (error) {
            next(error);
        }
    }
}

export const ordersController = new OrdersController();
