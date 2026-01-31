import { orderService } from './orderService';

describe('OrderService Unit Tests', () => {
  it('should create an order with correct total amount (MOCK mode)', async () => {
    const input = {
      customerId: '123e4567-e89b-12d3-a456-426614174000', // UUID válido
      items: [
        { productId: '111', quantity: 2, unitPrice: 50 },
        { productId: '222', quantity: 1, unitPrice: 100 }
      ]
    };

    const result = await orderService.createOrder(input);

    expect(result).toHaveProperty('id');
    expect(result.status).toBe('CREATED');
    // 2*50 + 1*100 = 200
    expect(result.totalAmount).toBe(200);
    expect(result.displayStatus).toBe('Pedido Criado'); // Mock behavior
  });
});
