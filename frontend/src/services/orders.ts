import { api } from './api'
import type {
  CreateOrderLog,
  Order,
  OrderLog,
  OrderStatus,
  SaveOrder,
  UpdateOrderStatus,
} from '@/types'

export const listOrders = (status?: OrderStatus) =>
  api
    .get<Order[]>('/api/orders', { params: status ? { status } : {} })
    .then((response) => response.data)

export const getOrder = (id: number) =>
  api.get<Order>(`/api/orders/${id}`).then((response) => response.data)

export const createOrder = (body: SaveOrder) =>
  api.post<Order>('/api/orders', body).then((response) => response.data)

export const updateOrder = (id: number, body: SaveOrder) =>
  api.put<Order>(`/api/orders/${id}`, body).then((response) => response.data)

export const updateOrderStatus = (id: number, body: UpdateOrderStatus) =>
  api.patch<Order>(`/api/orders/${id}/status`, body).then((response) => response.data)

export const listOrderLogs = (orderId: number) =>
  api.get<OrderLog[]>(`/api/orders/${orderId}/logs`).then((response) => response.data)

export const createOrderLog = (orderId: number, body: CreateOrderLog) =>
  api.post<OrderLog>(`/api/orders/${orderId}/logs`, body).then((response) => response.data)
