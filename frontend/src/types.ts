export const SPECIALIZATIONS = ['CUTTING', 'SEWING', 'FINISHING'] as const
export type Specialization = (typeof SPECIALIZATIONS)[number]
export type OrderStage = Specialization

export const ORDER_STATUSES = [
  'CREATED',
  'IN_PROGRESS',
  'READY_FOR_PICKUP',
  'PICKED_UP',
  'COMPLETED',
  'CANCELLED',
] as const
export type OrderStatus = (typeof ORDER_STATUSES)[number]

export const ORDER_ACTIONS = [
  'CREATED',
  'ASSIGNED',
  'STARTED',
  'COMPLETED',
  'READY_FOR_PICKUP',
  'PICKED_UP',
  'PAYMENT_RECORDED',
] as const
export type OrderAction = (typeof ORDER_ACTIONS)[number]

export interface Worker {
  id: number
  name: string
  specialization: Specialization
  active: boolean
  createdAt: string
}

export interface Order {
  id: number
  customerName: string
  status: OrderStatus
  stage: OrderStage | null
  totalPrice: number | null
  createdAt: string
  updatedAt: string
}

export interface OrderLog {
  id: number
  orderId: number
  workerId: number | null
  stage: OrderStage | null
  action: OrderAction
  note: string | null
  createdAt: string
}

export interface SaveWorker {
  name: string
  specialization: Specialization
  active?: boolean
}

export interface SaveOrder {
  customerName: string
  totalPrice: number | null
}

export interface UpdateOrderStatus {
  status: OrderStatus
  stage: OrderStage | null
}

export interface CreateOrderLog {
  action: OrderAction
  stage?: OrderStage | null
  workerId?: number | null
  note?: string | null
}
