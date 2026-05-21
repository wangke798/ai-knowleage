import request from '@/lib/request'
import type { Result } from '@/types/api'
import type { ChatMessage, Conversation } from '@/types/chat'

export interface ConversationCreateRequest {
  kbId: string
  title?: string
}

export const chatApi = {
  listConversations: async () => {
    const res = (await request.get('/chat/conversations')) as unknown as Result<Conversation[]>
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

  listMessages: async (conversationId: string) => {
    const res = (await request.get(`/chat/conversations/${conversationId}/messages`)) as unknown as Result<ChatMessage[]>
    return res.data
  },
}

/** 后端流式接口的完整 URL（用于 fetch-event-source）。 */
export const CHAT_STREAM_URL = '/api/chat/stream'
