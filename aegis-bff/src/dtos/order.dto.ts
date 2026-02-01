import { z } from 'zod';

// --- DTOs de Entrada (Vindo do Frontend) ---
export const CreateOrderSchema = z.object({
    items: z.array(z.object({
        productId: z.string().uuid(),
        quantity: z.number().int().positive(),
        unitPrice: z.number().positive(), // Frontend pode mandar, mas BFF deve validar/recalcular se possível
    })).min(1),
    customerId: z.string().uuid(),
    // Frontend envia data em formato ISO simples
    date: z.string().datetime().optional(),
});

export type CreateOrderInput = z.infer<typeof CreateOrderSchema>;

// --- DTOs de Saída (Para o Frontend) ---
export const OrderResponseSchema = z.object({
    id: z.string().uuid(),
    status: z.enum(['CREATED', 'PAID', 'CANCELLED']),
    totalAmount: z.number(),
    createdAt: z.string().datetime(),
    itemsCount: z.number(),
    // Campos amigáveis para UI
    displayStatus: z.string(),
});

export type OrderResponse = z.infer<typeof OrderResponseSchema>;
