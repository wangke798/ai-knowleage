import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { useCurrentUser } from '@/features/auth/hooks/useAuth'

export function ProtectedRoute() {
  const token = useAuthStore((s) => s.accessToken)
  const userInfo = useAuthStore((s) => s.userInfo)
  const location = useLocation()

  // 已有 token 但缺 userInfo 时，拉一次 /auth/me 同步
  useCurrentUser(!!token && !userInfo)

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  return <Outlet />
}
