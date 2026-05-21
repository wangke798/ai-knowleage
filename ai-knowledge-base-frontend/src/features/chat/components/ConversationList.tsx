import { Link, useNavigate, useParams } from 'react-router-dom'
import { MessageSquare, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useConversations, useDeleteConversation } from '../hooks/useChat'

interface Props {
  onPickNew?: () => void
}

export function ConversationList({ onPickNew }: Props) {
  const { conversationId } = useParams()
  const activeId = conversationId
  const navigate = useNavigate()
  const { data, isLoading } = useConversations()
  const deleteMut = useDeleteConversation()

  const handleDelete = async (id: string) => {
    if (!confirm('确定删除此会话？删除后无法恢复')) return
    await deleteMut.mutateAsync(id)
    if (activeId === id) navigate('/chat')
  }

  return (
    <div className="flex h-full flex-col gap-2">
      <Button variant="default" className="w-full" onClick={onPickNew}>
        + 新建对话
      </Button>

      <div className="flex-1 overflow-y-auto -mx-2 px-2 space-y-1">
        {isLoading ? (
          <>
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
            <Skeleton className="h-9 w-full" />
          </>
        ) : !data || data.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center">还没有对话，点上方新建</p>
        ) : (
          data.map((c) => {
            const isActive = c.id === activeId
            return (
              <div
                key={c.id}
                className={`group flex items-center gap-1 rounded-md px-2 py-1.5 text-sm cursor-pointer ${
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
                </Link>
                <button
                  type="button"
                  onClick={(e) => {
                    e.preventDefault()
                    handleDelete(c.id)
                  }}
                  className="opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-opacity"
                  title="删除"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
