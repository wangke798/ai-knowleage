import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar?: string
  email?: string
  roles: string[]
}

interface AuthState {
  accessToken: string | null
  accessTokenExpireAt: number | null
  refreshToken: string | null
  refreshTokenExpireAt: number | null
  userInfo: UserInfo | null

  setAuth: (payload: {
    accessToken: string
    accessTokenExpireAt?: number | null
    refreshToken?: string | null
    refreshTokenExpireAt?: number | null
    user: UserInfo
  }) => void
  setAccessToken: (token: string, expireAt?: number | null) => void
  setUser: (user: UserInfo) => void
  clearAuth: () => void
  isAdmin: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      accessTokenExpireAt: null,
      refreshToken: null,
      refreshTokenExpireAt: null,
      userInfo: null,

      setAuth: ({ accessToken, accessTokenExpireAt, refreshToken, refreshTokenExpireAt, user }) =>
        set({
          accessToken,
          accessTokenExpireAt: accessTokenExpireAt ?? null,
          refreshToken: refreshToken ?? null,
          refreshTokenExpireAt: refreshTokenExpireAt ?? null,
          userInfo: user,
        }),

      setAccessToken: (token, expireAt) =>
        set({ accessToken: token, accessTokenExpireAt: expireAt ?? null }),

      setUser: (user) => set({ userInfo: user }),

      clearAuth: () =>
        set({
          accessToken: null,
          accessTokenExpireAt: null,
          refreshToken: null,
          refreshTokenExpireAt: null,
          userInfo: null,
        }),

      isAdmin: () => get().userInfo?.roles.includes('ADMIN') ?? false,
    }),
    { name: 'auth-storage' },
  ),
)
