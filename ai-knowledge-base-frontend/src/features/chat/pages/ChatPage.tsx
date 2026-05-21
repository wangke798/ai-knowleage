import { useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConversationList } from '../components/ConversationList'
import { NewConversationDialog } from '../components/NewConversationDialog'
import { MessageBubble } from '../components/MessageBubble'
import { ChatInput } from '../components/ChatInput'
import { conversationKeys, useConversation, useMessages } from '../hooks/useChat'
import { useChatStream } from '../hooks/useChatStream'
import type { ChatMessage } from '@/types/chat'

export function ChatPage() {
  const { conversationId } = useParams()
  const id = conversationId
  const { data: conversation, isLoading: convLoading, isError: convError } = useConversation(id)
  const { data: serverMessages, isLoading: msgLoading } = useMessages(id)

  const qc = useQueryClient()
  const [newDialogOpen, setNewDialogOpen] = useState(false)
  const [input, setInput] = useState('')
  const [localMessages, setLocalMessages] = useState<ChatMessage[]>([])
  const scrollRef = useRef<HTMLDivElement>(null)

  // 每次切换会话或服务端消息刷新，用服务端数据覆盖本地（保证幂等）
  useEffect(() => {
    setLocalMessages(serverMessages ?? [])
  }, [serverMessages, id])

  const { ask, stop, pendingUser, streaming, isStreaming, error } = useChatStream({
    conversationId: id,
    onPersistedUserMessage: (m) => {
      // 已入库的 USER 消息追加到列表（替换 pendingUser）
      setLocalMessages((prev) => [...prev, m])
    },
    onPersistedAssistantMessage: (m) => {
      setLocalMessages((prev) => [...prev, m])
      // 同步刷新会话列表（更新时间）
      qc.invalidateQueries({ queryKey: conversationKeys.list() })
      if (id) qc.invalidateQueries({ queryKey: conversationKeys.messages(id) })
    },
  })

  const messagesToRender = useMemo(() => {
    const list = [...localMessages]
    if (pendingUser) {
      list.push({
        id: pendingUser.tempId,
        conversationId: id ?? '',
        role: 'USER',
        content: pendingUser.content,
        createTime: new Date().toISOString(),
      })
    }
    if (streaming) {
      list.push({
        id: streaming.tempId,
        conversationId: id ?? '',
        role: 'ASSISTANT',
        content: streaming.content,
        citations: streaming.citations,
        createTime: new Date().toISOString(),
      })
    }
    return list
  }, [localMessages, pendingUser, streaming, id])

  // 自动滚到底部（用户在流式期间）
  useEffect(() => {
    const el = scrollRef.current
    if (!el) return
    el.scrollTop = el.scrollHeight
  }, [messagesToRender.length, streaming?.content])

  const handleSend = () => {
    const v = input.trim()
    if (!v) return
    setInput('')
    ask(v)
  }

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-4">
      <aside className="w-64 border-r pr-4 hidden md:block">
        <ConversationList onPickNew={() => setNewDialogOpen(true)} />
      </aside>

      <main className="flex-1 flex flex-col min-w-0">
        {convLoading ? (
          <Skeleton className="h-8 w-64 mb-3" />
        ) : convError || !conversation ? (
          <EmptyState title="会话不存在" description="可能已被删除或您没有权限访问" />
        ) : (
          <>
            <div className="border-b pb-3 mb-3">
              <div className="text-base font-semibold truncate">{conversation.title}</div>
              {conversation.kbName && (
                <div className="text-xs text-muted-foreground mt-0.5">知识库：{conversation.kbName}</div>
              )}
            </div>

            <div ref={scrollRef} className="flex-1 overflow-y-auto -mx-2 px-2 space-y-4">
              {msgLoading ? (
                <Skeleton className="h-24 w-2/3" />
              ) : messagesToRender.length === 0 ? (
                <p className="text-sm text-muted-foreground py-12 text-center">
                  在下方输入问题开始对话。回答会基于「{conversation.kbName ?? '当前知识库'}」的文档内容。
                </p>
              ) : (
                messagesToRender.map((m, idx) => (
                  <MessageBubble
                    key={`${m.id}-${idx}`}
                    role={m.role}
                    content={m.content}
                    citations={m.citations}
                    streaming={m.id === streaming?.tempId && isStreaming}
                  />
                ))
              )}
              {error && (
                <div className="rounded-md border border-destructive/40 bg-destructive/5 px-3 py-2 text-sm text-destructive">
                  {error}
                </div>
              )}
            </div>

            <div className="mt-3">
              <ChatInput
                value={input}
                onChange={setInput}
                onSend={handleSend}
                onStop={stop}
                isStreaming={isStreaming}
              />
              <p className="mt-1.5 text-[11px] text-muted-foreground text-center">
                Enter 发送 · Shift+Enter 换行 · 回答由 AI 生成，可能存在错误
              </p>
            </div>
          </>
        )}
      </main>

      <NewConversationDialog open={newDialogOpen} onOpenChange={setNewDialogOpen} />
    </div>
  )
}
