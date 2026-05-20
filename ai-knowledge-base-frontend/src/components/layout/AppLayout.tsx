import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Header } from './Header'
import { useUIStore } from '@/stores/uiStore'
import { cn } from '@/lib/utils'

export function AppLayout() {
  const collapsed = useUIStore((s) => s.sidebarCollapsed)

  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar />
      <div className={cn('flex flex-col flex-1 overflow-hidden transition-all', collapsed ? 'ml-16' : 'ml-56')}>
        <Header />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
