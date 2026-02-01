import { orderService } from '../services/orderService';
import { v4 as uuidv4 } from 'uuid';

async function test() {
    const result = await orderService.createOrder({
        customerId: uuidv4(),
        items: [{ productId: uuidv4(), quantity: 2, unitPrice: 50.0 }],
    });
    console.log('Service Result:', JSON.stringify(result, null, 2));
}

test();
