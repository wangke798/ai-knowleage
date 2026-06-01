import type { UserInfo } from '@/stores/authStore'

export interface LoginRequest {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
}

export interface LoginResponse {
  accessToken: string
  accessTokenExpireAt: number
  refreshToken: string
  refreshTokenExpireAt: number
  user: UserInfo
}

export interface CaptchaResponse {
  captchaId: string
  captchaImage: string
}
