import { ref } from 'vue'
import { api } from './api'

export interface Admin {
  username: string
}

export const currentAdmin = ref<Admin | null>(null)
export const authChecked = ref(false)

export async function login(username: string, password: string) {
  const { data } = await api.post<Admin>('/api/auth/login', { username, password })
  currentAdmin.value = data
  authChecked.value = true
  return data
}

export async function logout() {
  await api.post('/api/auth/logout')
  currentAdmin.value = null
}

export async function fetchMe() {
  const { data } = await api.get<Admin>('/api/auth/me')
  currentAdmin.value = data
  return data
}
