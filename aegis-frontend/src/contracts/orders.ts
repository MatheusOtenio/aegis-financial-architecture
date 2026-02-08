import { z } from "zod";

export const ItemSchema = z
  .object({
    productId: z.string().uuid(),
    quantity: z.number().int().min(1),
    unitPrice: z.number().gt(0),
  })
  .strict();

export type ItemDTO = z.infer<typeof ItemSchema>;

export const CreateOrderInputSchema = z
  .object({
    customerId: z.string().uuid(),
    items: z.array(ItemSchema).min(1),
    date: z.string().datetime().optional(),
  })
  .strict();

export type CreateOrderInput = z.infer<typeof CreateOrderInputSchema>;

export const OrderDTOSchema = z
  .object({
    id: z.string().uuid(),
    status: z.enum(["CREATED", "PAID", "CANCELLED"]),
    totalAmount: z.number(),
    createdAt: z.string().datetime(),
    itemsCount: z.number().int().min(0),
    displayStatus: z.string(),
  })
  .strict();

export type OrderDTO = z.infer<typeof OrderDTOSchema>;

export const OrderSummaryDTOSchema = z
  .object({
    id: z.string(),
    total: z.number(),
    date: z.string().datetime(),
    status: z.string(),
  })
  .strict();

export type OrderSummaryDTO = z.infer<typeof OrderSummaryDTOSchema>;

export const ListOrdersResponseSchema = z
  .object({
    data: z.array(OrderSummaryDTOSchema),
    meta: z
      .object({
        date: z.string(),
        source: z.string(),
        count: z.number().int().min(0),
      })
      .strict(),
  })
  .strict();

export type ListOrdersResponse = z.infer<typeof ListOrdersResponseSchema>;

