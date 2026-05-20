import { useCallback, useRef, useState } from 'react'
import { CloudUpload, Loader2 } from 'lucide-react'
import { AxiosError } from 'axios'
import { Button } from '@/components/ui/button'
import { useUploadDocument } from '../hooks/useDocuments'
import type { Result } from '@/types/api'

interface DocumentUploaderProps {
  kbId: number
  disabled?: boolean
  /** 接受的扩展名，逗号分隔 */
  accept?: string
}

type Item = {
  id: string
  file: File
  percent: number
  status: 'uploading' | 'done' | 'failed'
  error?: string
}

const DEFAULT_ACCEPT = '.pdf,.doc,.docx,.ppt,.pptx,.txt,.md,.markdown,.html,.htm'

export function DocumentUploader({ kbId, disabled, accept = DEFAULT_ACCEPT }: DocumentUploaderProps) {
  const upload = useUploadDocument(kbId)
  const inputRef = useRef<HTMLInputElement>(null)
  const [items, setItems] = useState<Item[]>([])
  const [isDragOver, setDragOver] = useState(false)

  const handleFiles = useCallback(
    (files: FileList | File[]) => {
      const list = Array.from(files)
      for (const file of list) {
        const id = crypto.randomUUID()
        setItems((prev) => [...prev, { id, file, percent: 0, status: 'uploading' }])

        upload.mutate(
          {
            file,
            onProgress: (p) =>
              setItems((prev) => prev.map((it) => (it.id === id ? { ...it, percent: p } : it))),
          },
          {
            onSuccess: () => {
              setItems((prev) =>
                prev.map((it) => (it.id === id ? { ...it, status: 'done', percent: 100 } : it)),
              )
              // 1.5 秒后从列表淡出
              setTimeout(() => {
                setItems((prev) => prev.filter((it) => it.id !== id))
              }, 1500)
            },
            onError: (err) => {
              const msg =
                (err as unknown as Result | undefined)?.message ??
                (err as AxiosError | undefined)?.message ??
                '上传失败'
              setItems((prev) =>
                prev.map((it) => (it.id === id ? { ...it, status: 'failed', error: msg } : it)),
              )
            },
          },
        )
      }
    },
    [upload],
  )

  return (
    <div className="space-y-3">
      <div
        onDragOver={(e) => {
          if (disabled) return
          e.preventDefault()
          setDragOver(true)
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(e) => {
          if (disabled) return
          e.preventDefault()
          setDragOver(false)
          if (e.dataTransfer.files.length) handleFiles(e.dataTransfer.files)
        }}
        onClick={() => !disabled && inputRef.current?.click()}
        className={[
          'rounded-md border-2 border-dashed p-8 text-center cursor-pointer transition-colors',
          isDragOver ? 'border-primary bg-primary/5' : 'border-border',
          disabled ? 'opacity-50 cursor-not-allowed' : 'hover:bg-muted/30',
        ].join(' ')}
      >
        <CloudUpload className="mx-auto h-8 w-8 text-muted-foreground" />
        <div className="mt-2 text-sm">
          点击或拖拽文件到此处上传
        </div>
        <div className="mt-1 text-xs text-muted-foreground">
          支持 PDF / Word / PowerPoint / TXT / Markdown / HTML，单文件 ≤ 50MB
        </div>
        <Button type="button" size="sm" variant="outline" className="mt-3" disabled={disabled}>
          选择文件
        </Button>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept={accept}
          className="hidden"
          onChange={(e) => {
            if (e.target.files?.length) handleFiles(e.target.files)
            e.target.value = ''
          }}
        />
      </div>

      {items.length > 0 && (
        <ul className="space-y-1.5">
          {items.map((it) => (
            <li key={it.id} className="rounded-md border px-3 py-2 text-sm">
              <div className="flex items-center justify-between gap-2">
                <span className="truncate">{it.file.name}</span>
                <span className="shrink-0 text-xs text-muted-foreground">
                  {it.status === 'uploading' && (
                    <span className="inline-flex items-center gap-1">
                      <Loader2 className="h-3 w-3 animate-spin" />
                      {it.percent}%
                    </span>
                  )}
                  {it.status === 'done' && <span className="text-emerald-600">已上传</span>}
                  {it.status === 'failed' && (
                    <span className="text-destructive">{it.error ?? '失败'}</span>
                  )}
                </span>
              </div>
              {it.status === 'uploading' && (
                <div className="mt-1.5 h-1 rounded bg-muted overflow-hidden">
                  <div
                    className="h-full bg-primary transition-all"
                    style={{ width: `${it.percent}%` }}
                  />
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
