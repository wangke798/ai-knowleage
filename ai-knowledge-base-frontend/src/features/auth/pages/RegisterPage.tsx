import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useRegister } from '../hooks/useAuth'

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
    } catch {
      // 错误提示由 axios 拦截器统一弹出 toast
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">创建账号 ✨</h1>
        <p className="text-sm text-muted-foreground">开启您的 AI 知识库之旅</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="reg-username">
            用户名 <span className="text-destructive">*</span>
          </Label>
          <Input
            id="reg-username"
            placeholder="3-32 位字母、数字、下划线"
            autoComplete="username"
            className="h-11"
            {...register('username')}
          />
          {errors.username && (
            <p className="text-xs text-destructive">{errors.username.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-nickname">昵称</Label>
          <Input id="reg-nickname" placeholder="可选" className="h-11" {...register('nickname')} />
          {errors.nickname && (
            <p className="text-xs text-destructive">{errors.nickname.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-email">邮箱</Label>
          <Input
            id="reg-email"
            type="email"
            placeholder="可选"
            autoComplete="email"
            className="h-11"
            {...register('email')}
          />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-password">
            密码 <span className="text-destructive">*</span>
          </Label>
          <Input
            id="reg-password"
            type="password"
            placeholder="至少 6 位"
            autoComplete="new-password"
            className="h-11"
            {...register('password')}
          />
          {errors.password && (
            <p className="text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reg-confirm">
            确认密码 <span className="text-destructive">*</span>
          </Label>
          <Input
            id="reg-confirm"
            type="password"
            placeholder="再次输入密码"
            autoComplete="new-password"
            className="h-11"
            {...register('confirmPassword')}
          />
          {errors.confirmPassword && (
            <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
          )}
        </div>

        {done && (
          <div className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-3 py-2">
            <p className="text-sm text-emerald-600">注册成功，正在跳转登录...</p>
          </div>
        )}

        <Button
          type="submit"
          className="w-full h-11 text-base"
          disabled={isSubmitting || registerMutation.isPending || done}
        >
          {registerMutation.isPending ? '提交中...' : '注 册'}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          已有账号？
          <Link to="/login" className="ml-1 font-medium text-primary hover:underline">
            返回登录
          </Link>
        </p>
      </form>
    </div>
  )
}
