import request from '@/lib/request'
import type { Result } from '@/types/api'
import type { UserInfo } from '@/stores/authStore'
import type { LoginRequest, LoginResponse, RegisterRequest } from './types'

export const authApi = {
  login: async (body: LoginRequest) => {
    const res = (await request.post('/auth/login', body)) as unknown as Result<LoginResponse>
    return res.data
  },

  register: async (body: RegisterRequest) => {
    const res = (await request.post('/auth/register', body)) as unknown as Result<UserInfo>
    return res.data
  },

  refresh: async () => {
    const res = (await request.post('/auth/refresh', {})) as unknown as Result<LoginResponse>
    return res.data
  },

  logout: async () => {
    await request.post('/auth/logout', {})
  },

  me: async () => {
    const res = (await request.get('/auth/me')) as unknown as Result<UserInfo>
    return res.data
  },
}
