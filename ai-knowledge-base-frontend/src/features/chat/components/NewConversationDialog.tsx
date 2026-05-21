import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { useKnowledgeBases } from '@/features/knowledge-base/hooks/useKnowledgeBases'
import { useCreateConversation } from '../hooks/useChat'

interface Props {
  open: boolean
  onOpenChange: (v: boolean) => void
  /** 预选 kbId（从 KbDetailPage 进来时） */
  presetKbId?: string
}

export function NewConversationDialog({ open, onOpenChange, presetKbId }: Props) {
  const [kbId, setKbId] = useState<string | undefined>(presetKbId)
  const { data: kbPage, isLoading } = useKnowledgeBases({ page: 1, size: 100 })
  const createMut = useCreateConversation()
  const navigate = useNavigate()

  useEffect(() => {
    if (open) {
      setKbId(presetKbId)
    }
  }, [open, presetKbId])

  const handleCreate = async () => {
    if (!kbId) return
    const c = await createMut.mutateAsync({ kbId })
    onOpenChange(false)
    navigate(`/chat/${c.id}`)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>新建对话</DialogTitle>
        </DialogHeader>

        <div className="space-y-3">
          <Label>选择知识库</Label>
          {isLoading ? (
            <p className="text-sm text-muted-foreground">加载中…</p>
          ) : !kbPage || kbPage.records.length === 0 ? (
            <p className="text-sm text-muted-foreground">还没有可用的知识库，请先创建或加入一个。</p>
          ) : (
            <div className="max-h-72 overflow-y-auto space-y-1.5">
              {kbPage.records.map((kb) => {
                const active = kb.id === kbId
                return (
                  <button
                    key={kb.id}
                    type="button"
                    onClick={() => setKbId(kb.id)}
                    className={`w-full text-left rounded-md border px-3 py-2 text-sm transition-colors ${
                      active ? 'border-primary bg-primary/5' : 'hover:bg-accent/40'
                    }`}
                  >
                    <div className="font-medium">{kb.name}</div>
                    {kb.description && (
                      <div className="text-xs text-muted-foreground line-clamp-1">{kb.description}</div>
                    )}
                  </button>
                )
              })}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button onClick={handleCreate} disabled={!kbId || createMut.isPending}>
            {createMut.isPending ? '创建中…' : '开始对话'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
