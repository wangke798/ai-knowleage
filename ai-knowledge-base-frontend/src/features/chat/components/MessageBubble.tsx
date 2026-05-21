import { useState } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import { Badge } from '@/components/ui/badge'
import { ChevronDown, ChevronUp, FileText, User as UserIcon, Bot } from 'lucide-react'
import type { Citation } from '@/types/chat'

interface Props {
  role: 'USER' | 'ASSISTANT'
  content: string
  citations?: Citation[]
  streaming?: boolean
}

export function MessageBubble({ role, content, citations, streaming }: Props) {
  const [showCitations, setShowCitations] = useState(false)
  const isUser = role === 'USER'

  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
      <div className="flex-shrink-0 mt-1">
        <div className={`h-8 w-8 rounded-full flex items-center justify-center ${isUser ? 'bg-primary text-primary-foreground' : 'bg-muted'}`}>
          {isUser ? <UserIcon className="h-4 w-4" /> : <Bot className="h-4 w-4" />}
        </div>
      </div>

      <div className={`flex flex-col gap-2 max-w-[80%] ${isUser ? 'items-end' : 'items-start'}`}>
        <div
          className={`rounded-lg px-4 py-2.5 text-sm leading-relaxed ${
            isUser ? 'bg-primary text-primary-foreground' : 'bg-muted'
          }`}
        >
          {isUser ? (
            <div className="whitespace-pre-wrap break-words">{content || ' '}</div>
          ) : (
            <div className="prose prose-sm dark:prose-invert max-w-none break-words">
              <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeHighlight]}>
                {content || (streaming ? '思考中…' : '（空回答）')}
              </ReactMarkdown>
              {streaming && content && (
                <span className="inline-block w-1.5 h-3.5 ml-0.5 bg-foreground/60 animate-pulse align-middle" />
              )}
            </div>
          )}
        </div>

        {!isUser && citations && citations.length > 0 && (
          <div className="w-full">
            <button
              type="button"
              onClick={() => setShowCitations((v) => !v)}
              className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground"
            >
              {showCitations ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
              引用片段 ({citations.length})
            </button>
            {showCitations && (
              <div className="mt-2 space-y-2">
                {citations.map((c, idx) => (
                  <div key={`${c.chunkId}-${idx}`} className="rounded-md border bg-background px-3 py-2 text-xs">
                    <div className="flex items-center gap-2 text-muted-foreground mb-1">
                      <Badge variant="outline" className="px-1.5 py-0">#{idx + 1}</Badge>
                      <FileText className="h-3 w-3" />
                      <span className="truncate">{c.docName ?? `文档 ${c.docId}`}</span>
                      <span>· 片段 {c.seq}</span>
                      {typeof c.score === 'number' && <span>· {c.score.toFixed(3)}</span>}
                    </div>
                    <p className="whitespace-pre-wrap text-foreground/80 leading-relaxed">{c.snippet}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
