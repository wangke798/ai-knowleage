import { useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useVirtualizer } from '@tanstack/react-virtual'
import { Download, MessageSquare, Search, Star, StarOff, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { useConversations, useDeleteConversation, useToggleFavorite } from '../hooks/useChat'
import { chatApi } from '../api'
import type { Conversation } from '@/types/chat'

interface Props {
  onPickNew?: () => void
}

export function ConversationList({ onPickNew }: Props) {
  const { conversationId } = useParams()
  const activeId = conversationId
  const navigate = useNavigate()

  const [keyword, setKeyword] = useState('')
  const [favOnly, setFavOnly] = useState(false)

  const { data, isLoading } = useConversations({ keyword: keyword || undefined, favoriteOnly: favOnly || undefined })
  const deleteMut = useDeleteConversation()
  const favMut = useToggleFavorite()

  const parentRef = useRef<HTMLDivElement>(null)

  const items: Conversation[] = data ?? []

  const rowVirtualizer = useVirtualizer({
    count: items.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 56,
    overscan: 5,
  })

  const handleDelete = async (id: string, e: React.MouseEvent) => {
    e.preventDefault()
    if (!confirm('确定删除此会话？删除后无法恢复')) return
    await deleteMut.mutateAsync(id)
    if (activeId === id) navigate('/chat')
  }

  const handleFavorite = (id: string, e: React.MouseEvent) => {
    e.preventDefault()
    favMut.mutate(id)
  }

  const handleExport = (id: string, e: React.MouseEvent) => {
    e.preventDefault()
    chatApi.exportConversation(id, 'markdown')
  }

  return (
    <div className="flex h-full flex-col gap-2">
      <Button variant="default" className="w-full" onClick={onPickNew}>
        + 新建对话
      </Button>

      {/* 搜索栏 */}
      <div className="relative">
        <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-muted-foreground" />
        <Input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="搜索会话…"
          className="pl-8 h-8 text-xs"
        />
      </div>

      {/* 收藏过滤 */}
      <button
        type="button"
        onClick={() => setFavOnly((v) => !v)}
        className={`inline-flex items-center gap-1.5 text-xs px-2 py-1 rounded-md transition-colors ${
          favOnly ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400' : 'text-muted-foreground hover:text-foreground'
        }`}
      >
        <Star className="h-3 w-3" />
        {favOnly ? '已收藏' : '全部会话'}
      </button>

      {/* 列表（虚拟滚动） */}
      <div ref={parentRef} className="flex-1 overflow-y-auto -mx-2 px-2">
        {isLoading ? (
          <div className="space-y-1">
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
          </div>
        ) : items.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center">
            {favOnly ? '暂无收藏会话' : '还没有对话，点上方新建'}
          </p>
        ) : (
          <div style={{ height: `${rowVirtualizer.getTotalSize()}px`, position: 'relative' }}>
            {rowVirtualizer.getVirtualItems().map((vItem) => {
              const c = items[vItem.index]
              const isActive = c.id === activeId
              return (
                <div
                  key={c.id}
                  data-index={vItem.index}
                  ref={rowVirtualizer.measureElement}
                  style={{ position: 'absolute', top: vItem.start, left: 0, right: 0 }}
                  className={`group flex items-center gap-1 rounded-md px-2 py-1.5 text-sm cursor-pointer mb-0.5 ${
                    isActive ? 'bg-accent text-accent-foreground' : 'hover:bg-accent/50'
                  }`}
                >
                  <Link to={`/chat/${c.id}`} className="flex-1 min-w-0 flex items-center gap-2">
                    <MessageSquare className="h-3.5 w-3.5 flex-shrink-0 text-muted-foreground" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate">{c.title || '未命名会话'}</div>
                      {c.kbName && (
                        <div className="truncate text-[10px] text-muted-foreground">{c.kbName}</div>
                      )}
                    </div>
                    {c.isFavorite && <Star className="h-3 w-3 text-yellow-500 flex-shrink-0" />}
                  </Link>

                  {/* 操作按钮（hover 显示） */}
                  <div className="opacity-0 group-hover:opacity-100 flex items-center gap-0.5">
                    <button
                      type="button"
                      onClick={(e) => handleFavorite(c.id, e)}
                      className="text-muted-foreground hover:text-yellow-500 transition-colors p-0.5"
                      title={c.isFavorite ? '取消收藏' : '收藏'}
                    >
                      {c.isFavorite
                        ? <StarOff className="h-3.5 w-3.5" />
                        : <Star className="h-3.5 w-3.5" />}
                    </button>
                    <button
                      type="button"
                      onClick={(e) => handleExport(c.id, e)}
                      className="text-muted-foreground hover:text-foreground transition-colors p-0.5"
                      title="导出 Markdown"
                    >
                      <Download className="h-3.5 w-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={(e) => handleDelete(c.id, e)}
                      className="text-muted-foreground hover:text-destructive transition-colors p-0.5"
                      title="删除"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
