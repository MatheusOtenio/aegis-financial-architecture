import { CreateOrderInput, OrderResponse } from '../dtos/order.dto';
import { backendClient } from './backendClient';
import { env } from '../config/env';
import { v4 as uuidv4 } from 'uuid'; // Instalar uuid se faltar: npm i uuid @types/uuid

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

export class OrderService {

    async createOrder(input: CreateOrderInput): Promise<OrderResponse> {
        if (env.USE_MOCKS) {
            console.log('⚠️ Using MOCK for createOrder');
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
