import { Request, Response, NextFunction } from 'express';
import { orderService } from '../services/orderService';
import { CreateOrderInput } from '../dtos/order.dto';
import { z } from 'zod';

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

    async list(req: Request, res: Response, next: NextFunction) {
        try {
            // Validação simples de query param (default para hoje se vazio)
            const date = typeof req.query.date === 'string'
                ? req.query.date
                : new Date().toISOString().split('T')[0];

            // Opcional: Validar formato YYYY-MM-DD
            const dateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be in YYYY-MM-DD format');
            const validatedDate = dateSchema.parse(date);

            const data = await orderService.listOrders(validatedDate);

            res.json({
                data,
                meta: {
                    date: validatedDate,
                    source: 'cache-first', // Apenas informativo
                    count: data.length
                }
            });
        } catch (error) {
            next(error);
        }
    }
}

export const ordersController = new OrdersController();