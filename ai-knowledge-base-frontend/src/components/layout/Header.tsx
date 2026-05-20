import { LogOut, User } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { useLogout } from '@/features/auth/hooks/useAuth'

export function Header() {
  const navigate = useNavigate()
  const userInfo = useAuthStore((s) => s.userInfo)
  const logoutMutation = useLogout()

  const handleLogout = async () => {
    try {
      await logoutMutation.mutateAsync()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  return (
    <header className="flex items-center justify-end h-14 px-6 border-b bg-background shrink-0 gap-2">
      <span className="text-sm text-muted-foreground">
        {userInfo?.nickname ?? userInfo?.username}
      </span>
      <Button variant="ghost" size="icon" onClick={() => navigate('/settings/profile')}>
        <User className="h-4 w-4" />
      </Button>
      <Button
        variant="ghost"
        size="icon"
        onClick={handleLogout}
        disabled={logoutMutation.isPending}
        title="退出登录"
      >
        <LogOut className="h-4 w-4" />
      </Button>
    </header>
  )
}
