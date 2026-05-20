import { useState } from 'react'
import { Plus, Search, BookOpen } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { useKnowledgeBases } from '../hooks/useKnowledgeBases'
import { KbCard } from '../components/KbCard'
import { KbUpsertDialog } from '../components/KbUpsertDialog'
import type { KnowledgeBase } from '@/types/kb'

export function KbListPage() {
  const [keyword, setKeyword] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<KnowledgeBase | undefined>(undefined)

  const { data, isLoading, isError, error } = useKnowledgeBases({
    page: 1,
    size: 50,
    keyword: keyword.trim() || undefined,
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-bold">知识库</h1>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="搜索知识库名称"
              className="pl-8 w-64"
            />
          </div>
          <Button onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" />
            新建
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      ) : isError ? (
        <EmptyState
          title="加载失败"
          description={(error as { message?: string })?.message ?? '请稍后重试'}
        />
      ) : !data || data.records.length === 0 ? (
        <EmptyState
          icon={<BookOpen className="h-12 w-12" />}
          title={keyword ? '未找到匹配的知识库' : '还没有知识库'}
          description={keyword ? '换个关键词试试' : '创建一个知识库以开始管理你的文档和对话'}
          action={
            !keyword && (
              <Button onClick={() => setCreateOpen(true)}>
                <Plus className="h-4 w-4" />
                创建知识库
              </Button>
            )
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {data.records.map((kb) => (
            <KbCard key={kb.id} kb={kb} onEdit={setEditTarget} />
          ))}
        </div>
      )}

      <KbUpsertDialog open={createOpen} onOpenChange={setCreateOpen} />
      <KbUpsertDialog
        open={!!editTarget}
        onOpenChange={(v) => !v && setEditTarget(undefined)}
        initial={editTarget}
      />
    </div>
  )
}
