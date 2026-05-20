import { fetchEventSource } from '@microsoft/fetch-event-source'

interface SSEOptions {
  url: string
  body: unknown
  onMessage: (chunk: string) => void
  onDone?: () => void
  onError?: (err: unknown) => void
  signal?: AbortSignal
}

/**
 * 封装 SSE 流式请求，攒 16ms 批量 flush setState，避免渲染抖动
 */
export async function streamChat({ url, body, onMessage, onDone, onError, signal }: SSEOptions) {
  let buffer = ''
  let flushTimer: ReturnType<typeof setTimeout> | null = null

  const flush = () => {
    if (buffer) {
      onMessage(buffer)
      buffer = ''
    }
    flushTimer = null
  }

  const scheduleFlush = (chunk: string) => {
    buffer += chunk
    if (!flushTimer) {
      flushTimer = setTimeout(flush, 16)
    }
  }

  const token = (() => {
    try {
      const { accessToken } = JSON.parse(
        localStorage.getItem('auth-storage') ?? '{}'
      )?.state ?? {}
      return accessToken ?? ''
    } catch {
      return ''
    }
  })()

  await fetchEventSource(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
    signal,
    onmessage(ev) {
      if (ev.data === '[DONE]') {
        flush()
        onDone?.()
      } else {
        scheduleFlush(ev.data)
      }
    },
    onerror(err) {
      flush()
      onError?.(err)
      throw err
    },
  })
}
