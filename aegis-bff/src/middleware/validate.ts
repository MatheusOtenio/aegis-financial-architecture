import { Request, Response, NextFunction } from 'express';
import { AnyZodObject, ZodError } from 'zod';

export const validate = (schema: AnyZodObject) =>
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            await schema.parseAsync(req.body);
            next();
        } catch (error) {
            if (error instanceof ZodError) {
                res.status(400).json({
                    error: 'Validation Error',
                    details: error.errors.map(e => ({ path: e.path, message: e.message }))
                });
                return; // Retorno explícito para TS/Express
            }
            next(error);
        }
    };
