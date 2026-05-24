import request from '@/lib/request'
import type { Result } from '@/types/api'
import type { ChatMessage, Conversation } from '@/types/chat'

export interface ConversationCreateRequest {
  kbId: string
  title?: string
}

export interface ConversationListQuery {
  keyword?: string
  favoriteOnly?: boolean
}

export const chatApi = {
  listConversations: async (params?: ConversationListQuery) => {
    const res = (await request.get('/chat/conversations', { params })) as unknown as Result<Conversation[]>
    return res.data
  },

  createConversation: async (body: ConversationCreateRequest) => {
    const res = (await request.post('/chat/conversations', body)) as unknown as Result<Conversation>
    return res.data
  },

  detailConversation: async (conversationId: string) => {
    const res = (await request.get(`/chat/conversations/${conversationId}`)) as unknown as Result<Conversation>
    return res.data
  },

  renameConversation: async (conversationId: string, title: string) => {
    const res = (await request.put(`/chat/conversations/${conversationId}`, { title })) as unknown as Result<Conversation>
    return res.data
  },

  deleteConversation: async (conversationId: string) => {
    await request.delete(`/chat/conversations/${conversationId}`)
  },

  toggleFavorite: async (conversationId: string) => {
    const res = (await request.post(`/chat/conversations/${conversationId}/favorite`)) as unknown as Result<{ isFavorite: boolean }>
    return res.data
  },

  exportConversation: (conversationId: string, format: 'markdown' | 'text' | 'json' = 'markdown') => {
    // 直接打开下载链接（浏览器级下载）
    const url = `/api/chat/conversations/${conversationId}/export?format=${format}`
    const a = document.createElement('a')
    a.href = url
    a.download = `conversation_${conversationId}.${format === 'json' ? 'json' : format === 'text' ? 'txt' : 'md'}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  },

  listMessages: async (conversationId: string) => {
    const res = (await request.get(`/chat/conversations/${conversationId}/messages`)) as unknown as Result<ChatMessage[]>
    return res.data
  },
}

/** 后端流式接口的完整 URL（用于 fetch-event-source）。 */
export const CHAT_STREAM_URL = '/api/chat/stream'
