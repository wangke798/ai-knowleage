import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { toast } from 'sonner'
import { useAuthStore } from '@/stores/authStore'
import { uuid } from '@/lib/utils'

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
  config.headers['X-Trace-Id'] = uuid().replace(/-/g, '')
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
  (response) => {
    // 后端业务异常默认返回 HTTP 200 + { code, message, data }，code !== 200 视为业务错误
    const body = response.data as { code?: number; message?: string } | undefined
    if (
      body &&
      typeof body === 'object' &&
      'code' in body &&
      body.code !== undefined &&
      body.code !== 200 &&
      body.code !== 0
    ) {
      const silent = (response.config as RetriableConfig & { _silent?: boolean })?._silent
      if (!silent) {
        toast.error(body.message || '请求失败，请稍后再试')
      }
      return Promise.reject(body)
    }
    return response.data
  },
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

    // 统一错误提示（跳过静默接口）
    const payload = error.response?.data as { message?: string; code?: number } | undefined
    const silent = (original as RetriableConfig & { _silent?: boolean })?._silent
    const isAuthMeRequest = url?.includes('/auth/me')

    if (!silent && status !== 401 && !isAuthMeRequest) {
      const message =
        payload?.message ||
        (status === 0 || error.code === 'ERR_NETWORK' ? '网络异常，请检查连接' : error.message) ||
        '请求失败，请稍后再试'
      toast.error(message)
    }

    return Promise.reject(error.response?.data ?? error)
  },
)

export default request
