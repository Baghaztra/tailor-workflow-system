import { api } from './api'
import type { Dashboard } from '@/types'

export const getDashboard = () =>
  api.get<Dashboard>('/api/dashboard').then((response) => response.data)
