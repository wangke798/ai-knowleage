import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api'
import type { LoginRequest, RegisterRequest } from '../types'
import { useAuthStore } from '@/stores/authStore'

export function useLogin() {
  const setAuth = useAuthStore((s) => s.setAuth)
  return useMutation({
    mutationFn: (body: LoginRequest) => authApi.login(body),
    onSuccess: (data) => {
      setAuth({
        accessToken: data.accessToken,
        accessTokenExpireAt: data.accessTokenExpireAt,
        refreshToken: data.refreshToken,
        refreshTokenExpireAt: data.refreshTokenExpireAt,
        user: data.user,
      })
    },
  })
}

export function useRegister() {
  return useMutation({
    mutationFn: (body: RegisterRequest) => authApi.register(body),
  })
}

export function useLogout() {
  const clearAuth = useAuthStore((s) => s.clearAuth)
  const qc = useQueryClient()
  return useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => {
      clearAuth()
      qc.clear()
    },
  })
}

export function useCurrentUser(enabled = true) {
  const setUser = useAuthStore((s) => s.setUser)
  return useQuery({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      const user = await authApi.me()
      setUser(user)
      return user
    },
    enabled,
    staleTime: 1000 * 60 * 5,
    retry: false,
  })
}
