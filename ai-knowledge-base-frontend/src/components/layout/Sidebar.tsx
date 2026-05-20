import { NavLink } from 'react-router-dom'
import { BookOpen, MessageSquare, Settings, ChevronLeft } from 'lucide-react'
import { useUIStore } from '@/stores/uiStore'
import { cn } from '@/lib/utils'

const navItems = [
  { to: '/kb',       icon: BookOpen,       label: '知识库' },
  { to: '/chat',     icon: MessageSquare,  label: '对话'   },
  { to: '/settings/profile', icon: Settings, label: '设置' },
]

export function Sidebar() {
  const { sidebarCollapsed, toggleSidebar } = useUIStore()

  return (
    <aside className={cn(
      'fixed inset-y-0 left-0 z-20 flex flex-col border-r bg-background transition-all',
      sidebarCollapsed ? 'w-16' : 'w-56'
    )}>
      {/* Logo */}
      <div className="flex items-center h-14 px-4 border-b gap-2">
        <BookOpen className="h-6 w-6 text-primary shrink-0" />
        {!sidebarCollapsed && <span className="font-semibold truncate">AI 知识库</span>}
      </div>

      {/* Nav */}
      <nav className="flex-1 p-2 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              cn('flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors',
                isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-muted')
            }
          >
            <Icon className="h-4 w-4 shrink-0" />
            {!sidebarCollapsed && <span>{label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Collapse toggle */}
      <button
        onClick={toggleSidebar}
        className="flex items-center justify-center h-10 border-t hover:bg-muted transition-colors"
      >
        <ChevronLeft className={cn('h-4 w-4 transition-transform', sidebarCollapsed && 'rotate-180')} />
      </button>
    </aside>
  )
}
