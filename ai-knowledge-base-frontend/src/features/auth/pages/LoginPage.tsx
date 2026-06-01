import { useEffect, useState, useCallback } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useLogin } from '../hooks/useAuth'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '../api'

const schema = z.object({
  username: z.string().min(3, '用户名至少 3 位').max(32),
  password: z.string().min(6, '密码至少 6 位').max(64),
  captchaCode: z.string().min(1, '请输入验证码').max(10),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const token = useAuthStore((s) => s.accessToken)
  const loginMutation = useLogin()

  // 验证码相关状态
  const [captchaId, setCaptchaId] = useState('')
  const [captchaImage, setCaptchaImage] = useState('')
  const [captchaLoading, setCaptchaLoading] = useState(false)

  const from = (location.state as { from?: string } | null)?.from ?? '/kb'

  useEffect(() => {
    if (token) navigate(from, { replace: true })
  }, [token, from, navigate])

  const refreshCaptcha = useCallback(async () => {
    try {
      setCaptchaLoading(true)
      const data = await authApi.getCaptcha()
      setCaptchaId(data.captchaId)
      setCaptchaImage(data.captchaImage)
    } catch (e) {
      // 静默失败，用户点击图片即可重试
      console.error('加载验证码失败', e)
    } finally {
      setCaptchaLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshCaptcha()
  }, [refreshCaptcha])

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '', captchaCode: '' },
  })

  const onSubmit = async (values: FormValues) => {
    try {
      await loginMutation.mutateAsync({
        ...values,
        captchaId,
      })
      navigate(from, { replace: true })
    } catch {
      // 错误提示由 axios 拦截器统一弹出 toast
      // 登录失败后刷新验证码并清空输入
      refreshCaptcha()
      reset((prev) => ({ ...prev, captchaCode: '' }))
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">欢迎回来 👋</h1>
        <p className="text-sm text-muted-foreground">请输入您的账号信息以继续</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="login-username">用户名</Label>
          <Input
            id="login-username"
            placeholder="请输入用户名"
            autoComplete="username"
            className="h-11"
            {...register('username')}
          />
          {errors.username && (
            <p className="text-xs text-destructive">{errors.username.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="login-password">密码</Label>
          <Input
            id="login-password"
            type="password"
            placeholder="请输入密码"
            autoComplete="current-password"
            className="h-11"
            {...register('password')}
          />
          {errors.password && (
            <p className="text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="login-captcha">验证码</Label>
          <div className="flex gap-2">
            <Input
              id="login-captcha"
              placeholder="请输入图中字符"
              autoComplete="off"
              className="h-11 flex-1"
              {...register('captchaCode')}
            />
            <button
              type="button"
              onClick={refreshCaptcha}
              disabled={captchaLoading}
              title="点击刷新验证码"
              className="h-11 w-[130px] rounded-md border bg-muted/30 overflow-hidden flex items-center justify-center hover:border-primary transition-colors disabled:opacity-50"
            >
              {captchaImage ? (
                <img
                  src={captchaImage}
                  alt="验证码"
                  className="h-full w-full object-cover"
                />
              ) : (
                <span className="text-xs text-muted-foreground">
                  {captchaLoading ? '加载中...' : '点击获取'}
                </span>
              )}
            </button>
          </div>
          {errors.captchaCode && (
            <p className="text-xs text-destructive">{errors.captchaCode.message}</p>
          )}
        </div>

        <Button
          type="submit"
          className="w-full h-11 text-base"
          disabled={isSubmitting || loginMutation.isPending}
        >
          {loginMutation.isPending ? '登录中...' : '登 录'}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          还没有账号？
          <Link to="/register" className="ml-1 font-medium text-primary hover:underline">
            立即注册
          </Link>
        </p>
      </form>
    </div>
  )
}
