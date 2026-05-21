import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Search, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { EmptyState } from '@/components/shared/EmptyState'
import { kbSearchApi, type KbSearchHit } from '../api'

interface Props {
  kbId: string
}

export function KbSearchPanel({ kbId }: Props) {
  const [q, setQ] = useState('')
  const [topK, setTopK] = useState(5)
  const [hits, setHits] = useState<KbSearchHit[] | null>(null)

  const mutation = useMutation({
    mutationFn: async () => {
      const query = q.trim()
      if (!query) {
        throw new Error('请输入查询内容')
      }
      return kbSearchApi.search(kbId, query, topK)
    },
    onSuccess: (data) => {
      setHits(data ?? [])
    },
  })

  const errMsg = mutation.isError ? (mutation.error as { message?: string })?.message ?? '检索失败' : null

  return (
    <div className="space-y-4">
      <form
        className="flex flex-wrap items-end gap-3"
        onSubmit={(e) => {
          e.preventDefault()
          mutation.mutate()
        }}
      >
        <div className="flex-1 min-w-[280px] space-y-1">
          <Label htmlFor="kb-search-q">查询</Label>
          <Input
            id="kb-search-q"
            placeholder="输入问题或关键词，按知识库内容做语义召回"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
        <div className="w-24 space-y-1">
          <Label htmlFor="kb-search-topk">TopK</Label>
          <Input
            id="kb-search-topk"
            type="number"
            min={1}
            max={20}
            value={topK}
            onChange={(e) => setTopK(Math.max(1, Math.min(20, Number(e.target.value) || 5)))}
          />
        </div>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
          检索
        </Button>
      </form>

      {errMsg && (
        <div className="rounded-md border border-destructive/40 bg-destructive/5 px-3 py-2 text-sm text-destructive">
          {errMsg}
        </div>
      )}

      {hits === null ? (
        <p className="text-xs text-muted-foreground">提交查询后将展示相似度最高的片段，用于校验向量化效果。</p>
      ) : hits.length === 0 ? (
        <EmptyState title="无结果" description="尝试换个关键词，或确认文档已解析完成（状态为已完成）" />
      ) : (        <div className="space-y-3">
          {hits.map((h, idx) => (
            <div key={`${h.chunkId}-${idx}`} className="rounded-md border p-3 space-y-2">
              <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                <Badge variant="secondary">#{idx + 1}</Badge>
                <span>score: {h.score?.toFixed(4) ?? '-'}</span>
                {h.docName && <span>· {h.docName}</span>}
                <span>· chunk {h.seq}</span>
              </div>
              <pre className="whitespace-pre-wrap break-words text-sm leading-relaxed">{h.content}</pre>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
