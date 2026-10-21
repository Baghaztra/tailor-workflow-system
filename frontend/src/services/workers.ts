import { api } from './api'
import type { SaveWorker, Worker } from '@/types'

export const listWorkers = (active?: boolean) =>
  api
    .get<Worker[]>('/api/workers', { params: active === undefined ? {} : { active } })
    .then((response) => response.data)

export const createWorker = (body: SaveWorker) =>
  api.post<Worker>('/api/workers', body).then((response) => response.data)

export const updateWorker = (id: number, body: SaveWorker) =>
  api.put<Worker>(`/api/workers/${id}`, body).then((response) => response.data)
