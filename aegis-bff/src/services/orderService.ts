import { CreateOrderInput, OrderResponse } from '../dtos/order.dto';
import { backendClient } from './backendClient';
import { env } from '../config/env';
import { v4 as uuidv4 } from 'uuid';
import { redis } from '../lib/redis';

// Interface do Backend Core (o que ele espera/retorna estritamente)
interface BackendOrderPayload {
    customer_id: string;
    items: Array<{ product_id: string; qty: number; price: number }>;
    order_date?: string;
}

interface BackendOrderResponse {
    id: string;
    status: 'CREATED' | 'PAID' | 'CANCELLED';
    total: number;
    created_at: string;
}

// Interface para Listagem
interface OrderSummary {
    id: string;
    total: number;
    date: string;
    status: string;
}

export class OrderService {

    async createOrder(input: CreateOrderInput): Promise<OrderResponse> {
        if (env.USE_MOCKS) {
            console.log('Using MOCK for createOrder');
            return {
                id: uuidv4(),
                status: 'CREATED',
                totalAmount: input.items.reduce((acc, item) => acc + (item.quantity * item.unitPrice), 0),
                createdAt: new Date().toISOString(),
                itemsCount: input.items.length,
                displayStatus: 'Pedido Criado',
            };
        }

        // Mapper: UI DTO -> Backend DTO
        const payload: BackendOrderPayload = {
            customer_id: input.customerId,
            items: input.items.map(i => ({
                product_id: i.productId,
                qty: i.quantity,
                price: i.unitPrice
            })),
            order_date: input.date
        };

        const { data } = await backendClient.post<BackendOrderResponse>('/orders', payload);

        // Mapper: Backend Response -> UI DTO
        return {
            id: data.id,
            status: data.status,
            totalAmount: data.total,
            createdAt: data.created_at,
            itemsCount: payload.items.length,
            displayStatus: this.mapStatusToLabel(data.status),
        };
    }

    async listOrders(date: string): Promise<OrderSummary[]> {
        const cacheKey = `bff:orders:${date}`;

        // 1. Tentar ler do Cache
        try {
            const cached = await redis.get(cacheKey);
            if (cached) {
                console.log(`[CACHE HIT] ${cacheKey}`);
                return JSON.parse(cached);
            }
        } catch (e) {
            console.warn('Redis unavailable, skipping cache read');
        }

        // 2. Buscar da Fonte (Mock ou Backend)
        let orders: OrderSummary[] = [];

        if (env.USE_MOCKS) {
            console.log('⚠️ Using MOCK for listOrders');
            orders = [
                { id: uuidv4(), total: 150.00, date, status: 'Pago' },
                { id: uuidv4(), total: 299.90, date, status: 'Aguardando Pagamento' },
            ];
        } else {
            // Exemplo de chamada real
            const { data } = await backendClient.get<BackendOrderResponse[]>(`/orders?date=${date}`);
            orders = data.map(o => ({
                id: o.id,
                total: o.total,
                date: o.created_at,
                status: this.mapStatusToLabel(o.status)
            }));
        }

        // 3. Salvar no Cache (TTL 10s)
        try {
            await redis.setex(cacheKey, 10, JSON.stringify(orders));
        } catch (e) {
            console.warn('Redis unavailable, skipping cache write');
        }

        return orders;
    }

    private mapStatusToLabel(status: string): string {
        const labels: Record<string, string> = {
            'CREATED': 'Aguardando Pagamento',
            'PAID': 'Pago',
            'CANCELLED': 'Cancelado'
        };
        return labels[status] || status;
    }
}

export const orderService = new OrderService();