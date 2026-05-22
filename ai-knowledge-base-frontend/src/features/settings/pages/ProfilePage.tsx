import { LightSidebar } from '@/components/layout/LightSidebar';

export function ProfilePage() {
  return (
    <div className="flex w-full h-[calc(100vh-2rem)] bg-gray-100 rounded-xl overflow-hidden">
      <LightSidebar />
      <div className="flex-1 p-10 bg-white dark:bg-zinc-950 rounded-2xl m-4 ml-0 shadow-sm border border-gray-200 relative overflow-auto">
        <div className="max-w-lg">
          <h1 className="text-2xl font-bold mb-6">个人资料</h1>
          {/* TODO: Phase 1 实现昵称/头像修改表单 */}
          <p className="text-muted-foreground text-sm">个人资料开发中..1111.</p>
        </div>
      </div>
    </div>
  )
}
