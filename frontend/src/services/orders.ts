import { api } from './api'
import type { Order, OrderLog, OrderStatus, Page, PaymentMethod, SaveOrder } from '@/types'

export interface OrderFilter {
  status?: OrderStatus
  search?: string
  page?: number
  size?: number
}

export const listOrders = (params: OrderFilter = {}) =>
  api.get<Page<Order>>('/api/orders', { params }).then((response) => response.data)

export const getOrder = (id: number) =>
  api.get<Order>(`/api/orders/${id}`).then((response) => response.data)

export const createOrder = (body: SaveOrder) =>
  api.post<Order>('/api/orders', body).then((response) => response.data)

export const updateOrder = (id: number, body: SaveOrder) =>
  api.put<Order>(`/api/orders/${id}`, body).then((response) => response.data)

const action = (id: number, name: string) =>
  api.post<Order>(`/api/orders/${id}/${name}`).then((response) => response.data)

export const assignOrder = (id: number, workerId: number) =>
  api
    .post<Order>(`/api/orders/${id}/assign`, { workerId })
    .then((response) => response.data)

export const startOrder = (id: number) => action(id, 'start')

export const completeOrder = (id: number) => action(id, 'complete')

export const pickupOrder = (id: number) => action(id, 'pickup')

export const payOrder = (id: number, body: { amount: number; method: PaymentMethod }) =>
  api.post<Order>(`/api/orders/${id}/payment`, body).then((response) => response.data)

export const cancelOrder = (id: number) => action(id, 'cancel')

export const listOrderLogs = (orderId: number) =>
  api.get<OrderLog[]>(`/api/orders/${orderId}/logs`).then((response) => response.data)
