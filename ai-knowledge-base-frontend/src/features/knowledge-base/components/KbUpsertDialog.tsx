import { useEffect, useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useCreateKb, useUpdateKb } from '../hooks/useKnowledgeBases'
import type { KnowledgeBase } from '@/types/kb'

interface KbUpsertDialogProps {
  open: boolean
  onOpenChange: (v: boolean) => void
  /** 传入即为编辑模式 */
  initial?: KnowledgeBase
}

export function KbUpsertDialog({ open, onOpenChange, initial }: KbUpsertDialogProps) {
  const isEdit = !!initial

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [embeddingModel, setEmbeddingModel] = useState('')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      setName(initial?.name ?? '')
      setDescription(initial?.description ?? '')
      setEmbeddingModel(initial?.embeddingModel ?? '')
      setError(null)
    }
  }, [open, initial])

  const createMutation = useCreateKb()
  const updateMutation = useUpdateKb(initial?.id ?? 0)
  const mutation = isEdit ? updateMutation : createMutation

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = name.trim()
    if (!trimmed) {
      setError('名称不能为空')
      return
    }
    const body = {
      name: trimmed,
      description: description.trim() || undefined,
      embeddingModel: embeddingModel.trim() || undefined,
    }
    mutation.mutate(body as never, {
      onSuccess: () => {
        onOpenChange(false)
      },
      onError: (err: unknown) => {
        const message = (err as { message?: string })?.message ?? '提交失败'
        setError(message)
      },
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEdit ? '编辑知识库' : '创建知识库'}</DialogTitle>
            <DialogDescription>
              {isEdit ? '修改知识库的基本信息。' : '新建一个知识库，您将自动成为所有者。'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="kb-name">名称 *</Label>
              <Input
                id="kb-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="例如：产品手册库"
                maxLength={128}
                autoFocus
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="kb-desc">描述</Label>
              <Textarea
                id="kb-desc"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="简要说明这个知识库的用途"
                maxLength={512}
                rows={3}
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="kb-embedding">Embedding 模型</Label>
              <Input
                id="kb-embedding"
                value={embeddingModel}
                onChange={(e) => setEmbeddingModel(e.target.value)}
                placeholder="留空则使用系统默认，例如 bge-m3"
                maxLength={64}
              />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              取消
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? '提交中...' : isEdit ? '保存' : '创建'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
