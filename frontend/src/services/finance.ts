import { api } from './api'
import type { FinanceReportRow, FinanceSummary, Payment } from '@/types'

export const getFinanceSummary = () =>
  api.get<FinanceSummary>('/api/finance/summary').then((response) => response.data)

export interface PaymentFilter {
  from?: string
  to?: string
}

export const listPayments = (params: PaymentFilter = {}) =>
  api.get<Payment[]>('/api/payments', { params }).then((response) => response.data)

export const getFinanceReport = (params: PaymentFilter = {}) =>
  api.get<FinanceReportRow[]>('/api/finance/report', { params }).then((response) => response.data)

export async function exportPayments(params: PaymentFilter = {}) {
  const response = await api.get<Blob>('/api/finance/export', {
    params,
    responseType: 'blob',
  })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = `payments-${params.from ?? 'all'}-${params.to ?? 'all'}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
