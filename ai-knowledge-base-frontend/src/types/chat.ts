export interface Conversation {
  id: number
  kbId: number
  title: string
  createTime: string
}

export interface ChatMessage {
  id: number
  conversationId: number
  role: 'USER' | 'ASSISTANT'
  content: string
  citationChunkIds?: number[]
  createTime: string
}
