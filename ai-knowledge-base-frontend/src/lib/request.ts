import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/authStore'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true, // 允许携带 refresh_token HttpOnly Cookie
})

// 请求拦截：携带 AccessToken + TraceId
request.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Trace-Id'] = crypto.randomUUID().replace(/-/g, '')
  return config
})

// ------- 401 自动刷新（单飞 refresh） -------

interface RetriableConfig extends InternalAxiosRequestConfig {
  _retried?: boolean
}

let refreshing: Promise<string | null> | null = null

function isRefreshUrl(url: string | undefined) {
  return !!url && url.includes('/auth/refresh')
}

function isAuthFreeUrl(url: string | undefined) {
  return !!url && (url.includes('/auth/login') || url.includes('/auth/register') || isRefreshUrl(url))
}

async function refreshAccessToken(): Promise<string | null> {
  const store = useAuthStore.getState()
  // 优先使用 cookie 中的 refresh_token；若本地 store 也保存了则一并发到 body 作为兜底
  const body = store.refreshToken ? { refreshToken: store.refreshToken } : undefined
  try {
    const resp = await axios.post('/api/auth/refresh', body, { withCredentials: true })
    const data = resp.data?.data
    if (!data?.accessToken) return null
    useAuthStore.getState().setAuth({
      accessToken: data.accessToken,
      accessTokenExpireAt: data.accessTokenExpireAt,
      refreshToken: data.refreshToken,
      refreshTokenExpireAt: data.refreshTokenExpireAt,
      user: data.user,
    })
    return data.accessToken
  } catch {
    return null
  }
}

request.interceptors.response.use(
  (response) => response.data,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined
    const status = error.response?.status
    const url = original?.url

    // 401 且非登录/刷新接口，尝试刷新一次
    if (status === 401 && original && !original._retried && !isAuthFreeUrl(url)) {
      original._retried = true
      refreshing = refreshing ?? refreshAccessToken().finally(() => {
        refreshing = null
      })
      const newToken = await refreshing
      if (newToken) {
        original.headers = original.headers ?? {}
        ;(original.headers as Record<string, string>).Authorization = `Bearer ${newToken}`
        return request(original as AxiosRequestConfig)
      }
      // 刷新失败 → 清登录态并跳登录
      useAuthStore.getState().clearAuth()
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error.response?.data ?? error)
  },
)

export default request
