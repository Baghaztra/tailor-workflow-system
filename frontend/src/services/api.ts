import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL

export const api = axios.create({
  baseURL,
  withCredentials: true,
})

const authApi = axios.create({
  baseURL,
  withCredentials: true,
})

let refreshPromise: Promise<void> | null = null

const isAuthEndpoint = (url: string) =>
  url.startsWith('/api/auth/login') ||
  url.startsWith('/api/auth/refresh') ||
  url.startsWith('/api/auth/logout')

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.assign(`/login?redirect=${redirect}`)
  }
}

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean }

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as RetryConfig | undefined
    const status = error.response?.status
    const url = config?.url ?? ''

    if (status === 401 && config && !config._retry && !isAuthEndpoint(url)) {
      config._retry = true
      try {
        refreshPromise ??= authApi
          .post('/api/auth/refresh')
          .then(() => undefined)
          .finally(() => {
            refreshPromise = null
          })
        await refreshPromise
        return api(config)
      } catch {
        redirectToLogin()
        return Promise.reject(error)
      }
    }

    if (status === 401 && !isAuthEndpoint(url)) {
      redirectToLogin()
    }

    return Promise.reject(error)
  },
)

export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; error?: string } | undefined
    return data?.message ?? data?.error ?? error.message
  }
  return 'Unexpected error'
}
