import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { streamSSE } from '@/lib/sse'
import { CHAT_STREAM_URL } from '../api'
import type { Citation, ChatMessage } from '@/types/chat'

export interface StreamingMessage {
  /** 临时本地 id（负数字符串），收到 assistant_message 事件后替换为后端 id */
  tempId: string
  content: string
  citations?: Citation[]
}

interface UseChatStreamArgs {
  /** 已有会话 id；首次提问可为 undefined，配合 kbId 自动新建 */
  conversationId?: string
  kbId?: string
  onConversationReady?: (id: string) => void
  onPersistedUserMessage?: (msg: ChatMessage) => void
  onPersistedAssistantMessage?: (msg: ChatMessage) => void
}

/**
 * RAG 流式对话 hook。
 * 调用 ask(question)：
 * - 立即把 user message 推入 pending
 * - 通过 SSE 接收 token，攒 16ms 批量 flush 到 streaming 内容里
 * - done 时把 streaming 转成正式 assistant message（含引用）
 */
export function useChatStream({
  conversationId,
  kbId,
  onConversationReady,
  onPersistedUserMessage,
  onPersistedAssistantMessage,
}: UseChatStreamArgs) {
  const [pendingUser, setPendingUser] = useState<{ tempId: string; content: string } | null>(null)
  const [streaming, setStreaming] = useState<StreamingMessage | null>(null)
  const [isStreaming, setIsStreaming] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const abortRef = useRef<AbortController | null>(null)
  const bufferRef = useRef<string>('')
  const flushTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const citationsRef = useRef<Citation[] | undefined>(undefined)

  const flushBuffer = useCallback(() => {
    if (!bufferRef.current) return
    const chunk = bufferRef.current
    bufferRef.current = ''
    setStreaming((prev) =>
      prev ? { ...prev, content: prev.content + chunk } : prev,
    )
  }, [])

  const scheduleFlush = useCallback(() => {
    if (flushTimerRef.current) return
    flushTimerRef.current = setTimeout(() => {
      flushTimerRef.current = null
      flushBuffer()
    }, 16)
  }, [flushBuffer])

  const stop = useCallback(() => {
    abortRef.current?.abort()
    abortRef.current = null
    setIsStreaming(false)
  }, [])

  const ask = useCallback(
    async (question: string) => {
      const q = question.trim()
      if (!q || isStreaming) return
      setError(null)
      const tempUserId = `tmp-u-${Date.now()}`
      const tempAssistantId = `tmp-a-${Date.now()}`
      setPendingUser({ tempId: tempUserId, content: q })
      setStreaming({ tempId: tempAssistantId, content: '', citations: undefined })
      setIsStreaming(true)
      bufferRef.current = ''
      citationsRef.current = undefined

      const controller = new AbortController()
      abortRef.current = controller

      try {
        await streamSSE({
          url: CHAT_STREAM_URL,
          body: {
            conversationId,
            kbId: conversationId ? undefined : kbId,
            question: q,
          },
          signal: controller.signal,
          onEvent: (name, data) => {
            switch (name) {
              case 'conversation': {
                const id = (data as { id: string })?.id
                if (id) onConversationReady?.(id)
                break
              }
              case 'user_message': {
                const id = (data as { id: string })?.id
                if (id) {
                  onPersistedUserMessage?.({
                    id,
                    conversationId: conversationId ?? '',
                    role: 'USER',
                    content: q,
                    createTime: new Date().toISOString(),
                  })
                  setPendingUser(null)
                }
                break
              }
              case 'citations': {
                const list = Array.isArray(data) ? (data as Citation[]) : []
                citationsRef.current = list
                setStreaming((prev) => (prev ? { ...prev, citations: list } : prev))
                break
              }
              case 'token': {
                const v = (data as { v?: string })?.v
                if (typeof v === 'string') {
                  bufferRef.current += v
                  scheduleFlush()
                }
                break
              }
              case 'assistant_message': {
                // 流即将结束，强制 flush
                if (flushTimerRef.current) {
                  clearTimeout(flushTimerRef.current)
                  flushTimerRef.current = null
                }
                flushBuffer()
                const payload = data as { id: string; content: string }
                if (payload?.id) {
                  onPersistedAssistantMessage?.({
                    id: payload.id,
                    conversationId: conversationId ?? '',
                    role: 'ASSISTANT',
                    content: payload.content,
                    citations: citationsRef.current,
                    createTime: new Date().toISOString(),
                  })
                }
                break
              }
              case 'error': {
                const msg = (data as { message?: string })?.message ?? '对话失败'
                setError(msg)
                break
              }
              default:
                break
            }
          },
          onDone: () => {
            if (flushTimerRef.current) {
              clearTimeout(flushTimerRef.current)
              flushTimerRef.current = null
            }
            flushBuffer()
            setStreaming(null)
            setIsStreaming(false)
            abortRef.current = null
          },
          onError: (e) => {
            const msg = (e as { message?: string })?.message ?? '对话连接失败'
            setError(msg)
          },
        })
      } catch (e) {
        const msg = (e as { message?: string })?.message ?? '对话失败'
        setError(msg)
      } finally {
        // 兜底关流
        if (flushTimerRef.current) {
          clearTimeout(flushTimerRef.current)
          flushTimerRef.current = null
        }
        setStreaming((prev) => {
          if (!prev) return prev
          return prev.content ? prev : null
        })
        setIsStreaming(false)
        abortRef.current = null
      }
    },
    [conversationId, kbId, isStreaming, scheduleFlush, flushBuffer, onConversationReady, onPersistedUserMessage, onPersistedAssistantMessage],
  )

  // 切换会话时清空 streaming/pending
  useEffect(() => {
    setPendingUser(null)
    setStreaming(null)
    setError(null)
    bufferRef.current = ''
    if (flushTimerRef.current) {
      clearTimeout(flushTimerRef.current)
      flushTimerRef.current = null
    }
  }, [conversationId])

  return useMemo(
    () => ({ ask, stop, pendingUser, streaming, isStreaming, error }),
    [ask, stop, pendingUser, streaming, isStreaming, error],
  )
}
