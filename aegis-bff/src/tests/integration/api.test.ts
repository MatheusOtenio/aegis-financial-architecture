import request from 'supertest';
import app from '../../server'; 

describe('API Integration Tests', () => {
  
  it('GET /health should return 200 OK', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('status', 'ok');
  });

  it('POST /api/orders should validate input (400 Bad Request)', async () => {
    const res = await request(app)
      .post('/api/orders')
      .send({ customerId: 'invalid-uuid' }); // Payload incompleto/inválido

    expect(res.status).toBe(400);
    expect(res.body.error).toBe('Validation Error');
  });

  it('POST /api/orders should create order (201 Created)', async () => {
    const validPayload = {
      customerId: '123e4567-e89b-12d3-a456-426614174000',
      items: [{ productId: '123e4567-e89b-12d3-a456-426614174001', quantity: 1, unitPrice: 99.90 }]
    };

    const res = await request(app)
      .post('/api/orders')
      .send(validPayload);

    expect(res.status).toBe(201);
    expect(res.body).toHaveProperty('id');
    expect(res.body.totalAmount).toBe(99.90);
  });
});
