import { api } from './api'
import type { Page, SaveWorker, Worker } from '@/types'

export interface WorkerFilter {
  active?: boolean
  search?: string
  page?: number
  size?: number
}

export const listWorkers = (params: WorkerFilter = {}) =>
  api.get<Page<Worker>>('/api/workers', { params }).then((response) => response.data)

export const createWorker = (body: SaveWorker) =>
  api.post<Worker>('/api/workers', body).then((response) => response.data)

export const updateWorker = (id: number, body: SaveWorker) =>
  api.put<Worker>(`/api/workers/${id}`, body).then((response) => response.data)
