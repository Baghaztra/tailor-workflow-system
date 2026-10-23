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

export type OrderAction =
  | 'CREATED'
  | 'ASSIGNED'
  | 'STARTED'
  | 'COMPLETED'
  | 'READY_FOR_PICKUP'
  | 'PICKED_UP'
  | 'PAYMENT_RECORDED'
  | 'CANCELLED'

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

export const PAYMENT_METHODS = ['CASH', 'TRANSFER'] as const
export type PaymentMethod = (typeof PAYMENT_METHODS)[number]

export interface Payment {
  id: number
  orderId: number
  customerName: string
  amount: number
  method: PaymentMethod
  paidAt: string
}

export interface FinanceSummary {
  totalRevenue: number
  monthRevenue: number
  todayRevenue: number
  paymentCount: number
}

export interface FinanceReportRow {
  date: string
  paymentCount: number
  revenue: number
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}


