import {
  CreateOrderInput,
  ListOrdersResponse,
  OrderDTO,
} from "@/contracts/orders";
import { apiFetch } from "./http-client";

export function postCreateOrder(
  input: CreateOrderInput
): Promise<OrderDTO> {
  return apiFetch<OrderDTO>("/api/orders", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getOrdersByDate(
  date: string
): Promise<ListOrdersResponse> {
  return apiFetch<ListOrdersResponse>(`/api/orders?date=${date}`);
}

