import { useRef, useState } from 'react'
import { useVirtualizer } from '@tanstack/react-virtual'
import { Download, RefreshCw, Trash2 } from 'lucide-react'
import { format } from 'date-fns'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { documentApi } from '../api'
import { useDeleteDocument, useDocuments, useReparseDocument } from '../hooks/useDocuments'
import type { KbDocument, ParseStatus } from '@/types/document'

interface DocumentTableProps {
  kbId: string
  canWrite: boolean
}

const statusMeta: Record<
  ParseStatus,
  { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }
> = {
  PENDING: { label: '待解析', variant: 'outline' },
  PROCESSING: { label: '解析中', variant: 'secondary' },
  DONE: { label: '已完成', variant: 'default' },
  FAILED: { label: '失败', variant: 'destructive' },
}

function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

const ROW_HEIGHT = 44

export function DocumentTable({ kbId, canWrite }: DocumentTableProps) {
  const [page, setPage] = useState(1)
  const size = 100  // 加大每页条数，配合虚拟滚动
  const { data, isLoading } = useDocuments(kbId, { page, size })
  const remove = useDeleteDocument(kbId)
  const reparse = useReparseDocument(kbId)
  const [pendingDelete, setPendingDelete] = useState<KbDocument | null>(null)
  const tbodyRef = useRef<HTMLDivElement>(null)

  const records = data?.records ?? []
  const total = data?.total ?? 0
  const totalPages = Math.max(1, Math.ceil(total / size))

  // 虚拟化行（仅当条目 > 30 时启用，否则直接渲染）
  const rowVirtualizer = useVirtualizer({
    count: records.length,
    getScrollElement: () => tbodyRef.current,
    estimateSize: () => ROW_HEIGHT,
    overscan: 5,
    enabled: records.length > 30,
  })

  if (isLoading) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-10" />
        <Skeleton className="h-10" />
        <Skeleton className="h-10" />
      </div>
    )
  }

  const renderRow = (d: KbDocument) => {
    const meta = statusMeta[d.parseStatus] ?? statusMeta.PENDING
    return (
      <>
        <td className="px-4 py-2 truncate max-w-xs" title={d.name}>{d.name}</td>
        <td className="px-4 py-2 text-muted-foreground">{formatSize(d.fileSize)}</td>
        <td className="px-4 py-2">
          <Badge variant={meta.variant} title={d.parseError ?? ''}>{meta.label}</Badge>
        </td>
        <td className="px-4 py-2 text-muted-foreground">{d.chunkCount ?? '-'}</td>
        <td className="px-4 py-2 text-muted-foreground">{d.uploaderName ?? `#${d.uploaderId}`}</td>
        <td className="px-4 py-2 text-muted-foreground">
          {d.createTime ? format(new Date(d.createTime), 'yyyy-MM-dd HH:mm') : '-'}
        </td>
        <td className="px-4 py-2">
          <div className="flex items-center gap-1">
            <a href={documentApi.downloadUrl(kbId, d.id)} target="_blank" rel="noreferrer">
              <Button variant="ghost" size="icon-sm" aria-label="下载">
                <Download className="h-4 w-4" />
              </Button>
            </a>
            {canWrite && (d.parseStatus === 'FAILED' || d.parseStatus === 'DONE') && (
              <Button
                variant="ghost"
                size="icon-sm"
                aria-label="重新解析"
                title="重新解析"
                disabled={reparse.isPending}
                onClick={() => reparse.mutate(d.id)}
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
            )}
            {canWrite && (
              <Button
                variant="ghost"
                size="icon-sm"
                aria-label="删除"
                onClick={() => setPendingDelete(d)}
              >
                <Trash2 className="h-4 w-4 text-destructive" />
              </Button>
            )}
          </div>
        </td>
      </>
    )
  }

  return (
    <div className="space-y-3">
      <div className="rounded-md border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-muted/40">
            <tr className="text-left">
              <th className="px-4 py-2 font-medium">文件名</th>
              <th className="px-4 py-2 font-medium w-24">大小</th>
              <th className="px-4 py-2 font-medium w-24">状态</th>
              <th className="px-4 py-2 font-medium w-24">切片</th>
              <th className="px-4 py-2 font-medium w-28">上传者</th>
              <th className="px-4 py-2 font-medium w-44">上传时间</th>
              <th className="px-4 py-2 font-medium w-28">操作</th>
            </tr>
          </thead>
        </table>

        {/* 虚拟滚动区域 */}
        <div
          ref={tbodyRef}
          className="overflow-y-auto"
          style={{ maxHeight: records.length > 30 ? '480px' : undefined }}
        >
          {records.length === 0 ? (
            <div className="px-4 py-8 text-center text-muted-foreground text-sm">暂无文档，请上传</div>
          ) : records.length <= 30 ? (
            // 条目少时直接渲染
            <table className="w-full text-sm">
              <tbody>
                {records.map((d) => (
                  <tr key={d.id} className="border-t">{renderRow(d)}</tr>
                ))}
              </tbody>
            </table>
          ) : (
            // 条目多时虚拟滚动
            <div style={{ height: `${rowVirtualizer.getTotalSize()}px`, position: 'relative' }}>
              {rowVirtualizer.getVirtualItems().map((vItem) => {
                const d = records[vItem.index]
                return (
                  <table
                    key={d.id}
                    className="w-full text-sm"
                    style={{ position: 'absolute', top: vItem.start, left: 0, right: 0 }}
                  >
                    <tbody>
                      <tr className="border-t">{renderRow(d)}</tr>
                    </tbody>
                  </table>
                )
              })}
            </div>
          )}
        </div>
      </div>

      {total > size && (
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>共 {total} 条</span>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>
              上一页
            </Button>
            <span>{page} / {totalPages}</span>
            <Button variant="outline" size="sm" disabled={page >= totalPages} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>
              下一页
            </Button>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={!!pendingDelete}
        onOpenChange={(v) => !v && setPendingDelete(null)}
        title="删除文档？"
        description={`即将删除「${pendingDelete?.name}」，对应文件和切片将一并清理。`}
        confirmText="删除"
        destructive
        loading={remove.isPending}
        onConfirm={() => {
          if (!pendingDelete) return
          remove.mutate(pendingDelete.id, {
            onSuccess: () => setPendingDelete(null),
          })
        }}
      />
    </div>
  )
}
