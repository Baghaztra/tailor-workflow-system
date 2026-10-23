import { api } from './api'
import type { FinanceSummary, Payment } from '@/types'

export const getFinanceSummary = () =>
  api.get<FinanceSummary>('/api/finance/summary').then((response) => response.data)

export interface PaymentFilter {
  from?: string
  to?: string
}

export const listPayments = (params: PaymentFilter = {}) =>
  api.get<Payment[]>('/api/payments', { params }).then((response) => response.data)
