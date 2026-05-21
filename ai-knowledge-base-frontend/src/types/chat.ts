export interface Conversation {
  id: string
  kbId: string
  kbName?: string
  title: string
  createTime: string
  updateTime?: string
}

export interface Citation {
  chunkId: string
  docId: string
  docName?: string
  seq: number
  snippet: string
  score?: number
}

export interface ChatMessage {
  id: string
  conversationId: string
  role: 'USER' | 'ASSISTANT'
  content: string
  citations?: Citation[]
  tokenCount?: number
  createTime: string
}
