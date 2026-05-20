import { useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { AxiosError } from 'axios'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useLogin } from '../hooks/useAuth'
import { useAuthStore } from '@/stores/authStore'
import type { Result } from '@/types/api'

const schema = z.object({
  username: z.string().min(3, '用户名至少 3 位').max(32),
  password: z.string().min(6, '密码至少 6 位').max(64),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const token = useAuthStore((s) => s.accessToken)
  const loginMutation = useLogin()

  const from = (location.state as { from?: string } | null)?.from ?? '/kb'

  useEffect(() => {
    if (token) navigate(from, { replace: true })
  }, [token, from, navigate])

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '' },
  })

  const onSubmit = async (values: FormValues) => {
    try {
      await loginMutation.mutateAsync(values)
      navigate(from, { replace: true })
    } catch (err) {
      const msg =
        (err as Result | undefined)?.message ??
        (err as AxiosError | undefined)?.message ??
        '登录失败，请稍后再试'
      setError('root', { message: msg })
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="rounded-xl border bg-card p-8 shadow-sm w-[360px]"
    >
      <h1 className="text-2xl font-bold mb-1 text-center">登录</h1>
      <p className="text-xs text-muted-foreground mb-6 text-center">
        AI 知识库 · 请使用账号登录
      </p>
      <div className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="login-username">用户名</Label>
          <Input id="login-username" autoComplete="username" {...register('username')} />
          {errors.username && (
            <p className="text-xs text-destructive">{errors.username.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="login-password">密码</Label>
          <Input
            id="login-password"
            type="password"
            autoComplete="current-password"
            {...register('password')}
          />
          {errors.password && (
            <p className="text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        {errors.root && (
          <p className="text-sm text-destructive text-center">{errors.root.message}</p>
        )}

        <Button type="submit" className="w-full" disabled={isSubmitting || loginMutation.isPending}>
          {loginMutation.isPending ? '登录中...' : '登录'}
        </Button>

        <p className="text-center text-xs text-muted-foreground">
          还没有账号？
          <Link to="/register" className="ml-1 text-primary hover:underline">
            立即注册
          </Link>
        </p>
      </div>
    </form>
  )
}
