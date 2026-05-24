import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, ChevronLeft, ChevronRight, ZoomIn, ZoomOut } from 'lucide-react'
import { Document, Page, pdfjs } from 'react-pdf'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { documentApi } from '../api'
import 'react-pdf/dist/Page/AnnotationLayer.css'
import 'react-pdf/dist/Page/TextLayer.css'

// 配置 PDF.js worker（使用 CDN）
pdfjs.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`

export function DocDetailPage() {
  const { kbId, docId } = useParams()
  const [numPages, setNumPages] = useState<number>(0)
  const [pageNumber, setPageNumber] = useState(1)
  const [scale, setScale] = useState(1.2)

  if (!kbId || !docId) return null

  const pdfUrl = documentApi.downloadUrl(kbId, docId)

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Link
          to={`/kb/${kbId}`}
          className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="mr-1 h-3.5 w-3.5" />
          返回知识库
        </Link>
      </div>

      {/* 工具栏 */}
      <div className="flex items-center gap-2 rounded-md border bg-muted/30 px-3 py-2">
        <Button
          variant="ghost"
          size="sm"
          disabled={pageNumber <= 1}
          onClick={() => setPageNumber((p) => Math.max(1, p - 1))}
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <span className="text-sm text-muted-foreground min-w-[80px] text-center">
          {pageNumber} / {numPages || '…'}
        </span>
        <Button
          variant="ghost"
          size="sm"
          disabled={pageNumber >= numPages}
          onClick={() => setPageNumber((p) => Math.min(numPages, p + 1))}
        >
          <ChevronRight className="h-4 w-4" />
        </Button>

        <div className="flex-1" />

        <Button
          variant="ghost"
          size="sm"
          onClick={() => setScale((s) => Math.max(0.5, s - 0.2))}
          title="缩小"
        >
          <ZoomOut className="h-4 w-4" />
        </Button>
        <span className="text-sm text-muted-foreground w-14 text-center">
          {Math.round(scale * 100)}%
        </span>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setScale((s) => Math.min(3, s + 0.2))}
          title="放大"
        >
          <ZoomIn className="h-4 w-4" />
        </Button>
      </div>

      {/* PDF 渲染区 */}
      <div className="flex justify-center rounded-md border bg-muted/20 p-4 overflow-auto min-h-[600px]">
        <Document
          file={pdfUrl}
          onLoadSuccess={({ numPages }) => {
            setNumPages(numPages)
            setPageNumber(1)
          }}
          loading={
            <div className="space-y-2 w-[600px]">
              <Skeleton className="h-[800px] w-full" />
            </div>
          }
          error={
            <div className="flex flex-col items-center justify-center gap-4 py-20 text-sm text-muted-foreground">
              <p>PDF 加载失败，可能该文件不是 PDF 格式，或需要登录后下载。</p>
              <a href={pdfUrl} target="_blank" rel="noreferrer">
                <Button variant="outline" size="sm">直接下载文件</Button>
              </a>
            </div>
          }
        >
          <Page
            pageNumber={pageNumber}
            scale={scale}
            renderTextLayer
            renderAnnotationLayer
          />
        </Document>
      </div>
    </div>
  )
}
