import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { AxiosError } from 'axios'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useRegister } from '../hooks/useAuth'
import type { Result } from '@/types/api'

const schema = z
  .object({
    username: z
      .string()
      .min(3, '用户名至少 3 位')
      .max(32, '用户名最多 32 位')
      .regex(/^[a-zA-Z0-9_-]+$/, '只允许字母、数字、下划线和短横线'),
    password: z.string().min(6, '密码至少 6 位').max(64, '密码最多 64 位'),
    confirmPassword: z.string().min(6, '请再次输入密码'),
    nickname: z.string().max(32, '昵称最多 32 位').optional().or(z.literal('')),
    email: z.string().email('邮箱格式不正确').optional().or(z.literal('')),
  })
  .refine((v) => v.password === v.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  })

type FormValues = z.infer<typeof schema>

export function RegisterPage() {
  const navigate = useNavigate()
  const registerMutation = useRegister()
  const [done, setDone] = useState(false)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '', confirmPassword: '', nickname: '', email: '' },
  })

  const onSubmit = async (values: FormValues) => {
    try {
      await registerMutation.mutateAsync({
        username: values.username,
        password: values.password,
        nickname: values.nickname || undefined,
        email: values.email || undefined,
      })
      setDone(true)
      setTimeout(() => navigate('/login', { replace: true }), 1200)
    } catch (err) {
      const msg =
        (err as Result | undefined)?.message ??
        (err as AxiosError | undefined)?.message ??
        '注册失败，请稍后再试'
      setError('root', { message: msg })
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="rounded-xl border bg-card p-8 shadow-sm w-[400px]"
    >
      <h1 className="text-2xl font-bold mb-1 text-center">注册</h1>
      <p className="text-xs text-muted-foreground mb-6 text-center">
        创建你的 AI 知识库账号
      </p>

      <div className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="reg-username">用户名 *</Label>
          <Input id="reg-username" autoComplete="username" {...register('username')} />
          {errors.username && (
            <p className="text-xs text-destructive">{errors.username.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-nickname">昵称</Label>
          <Input id="reg-nickname" {...register('nickname')} />
          {errors.nickname && (
            <p className="text-xs text-destructive">{errors.nickname.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-email">邮箱</Label>
          <Input id="reg-email" type="email" autoComplete="email" {...register('email')} />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-password">密码 *</Label>
          <Input
            id="reg-password"
            type="password"
            autoComplete="new-password"
            {...register('password')}
          />
          {errors.password && (
            <p className="text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-confirm">确认密码 *</Label>
          <Input
            id="reg-confirm"
            type="password"
            autoComplete="new-password"
            {...register('confirmPassword')}
          />
          {errors.confirmPassword && (
            <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
          )}
        </div>

        {errors.root && (
          <p className="text-sm text-destructive text-center">{errors.root.message}</p>
        )}
        {done && (
          <p className="text-sm text-emerald-600 text-center">注册成功，正在跳转登录...</p>
        )}

        <Button
          type="submit"
          className="w-full"
          disabled={isSubmitting || registerMutation.isPending || done}
        >
          {registerMutation.isPending ? '提交中...' : '注册'}
        </Button>

        <p className="text-center text-xs text-muted-foreground">
          已有账号？
          <Link to="/login" className="ml-1 text-primary hover:underline">
            返回登录
          </Link>
        </p>
      </div>
    </form>
  )
}
