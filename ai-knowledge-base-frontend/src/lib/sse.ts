import { fetchEventSource } from '@microsoft/fetch-event-source'

interface SSEEventOptions {
  url: string
  body: unknown
  /**
   * 按事件名分发的处理器。事件名对应后端 SseEmitter.event().name(...) 的 `event:` 字段。
   * `data` 为已解析过的 JSON（解析失败时为原字符串）。
   */
  onEvent: (name: string, data: unknown) => void
  onDone?: () => void
  onError?: (err: unknown) => void
  signal?: AbortSignal
}

function readAccessToken(): string {
  try {
    const raw = localStorage.getItem('auth-storage')
    if (!raw) return ''
    const parsed = JSON.parse(raw) as { state?: { accessToken?: string } }
    return parsed?.state?.accessToken ?? ''
  } catch {
    return ''
  }
}

/**
 * 通用 SSE 客户端：POST + Authorization 头，按事件名回调。
 * 与原 streamChat 不同，这里把多种事件（conversation / citations / token / done / error）
 * 全部透传给上层，由 UI 决定怎么处理。
 */
export async function streamSSE({ url, body, onEvent, onDone, onError, signal }: SSEEventOptions) {
  const token = readAccessToken()
  await fetchEventSource(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(body),
    signal,
    openWhenHidden: true,
    async onopen(res) {
      if (!res.ok) {
        let msg = `SSE 连接失败 (${res.status})`
        try {
          const j = await res.json()
          if (j && typeof j === 'object' && 'message' in j) msg = String((j as { message: string }).message)
        } catch {
          /* ignore */
        }
        throw new Error(msg)
      }
    },
    onmessage(ev) {
      const name = ev.event || 'message'
      let payload: unknown = ev.data
      if (ev.data && (ev.data.startsWith('{') || ev.data.startsWith('['))) {
        try { payload = JSON.parse(ev.data) } catch { /* keep raw */ }
      }
      onEvent(name, payload)
      if (name === 'done') onDone?.()
    },
    onerror(err) {
      onError?.(err)
      throw err
    },
  })
}
