import request from '@/lib/request'
import type { PageResult, Result } from '@/types/api'
import type {
  KbMember,
  KbMemberAddRequest,
  KbUpsertRequest,
  KbRole,
  KnowledgeBase,
} from '@/types/kb'

// 后端统一返回 Result<T>，axios 响应拦截器已经把 response.data 解包为 Result<T>
// 这里再剥一层 .data 给上层使用。

export interface KbPageQuery {
  page?: number
  size?: number
  keyword?: string
}

export const kbApi = {
  page: async (params: KbPageQuery = {}) => {
    const res = (await request.get('/kb', { params })) as unknown as Result<PageResult<KnowledgeBase>>
    return res.data
  },

  detail: async (kbId: string) => {
    const res = (await request.get(`/kb/${kbId}`)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  create: async (body: KbUpsertRequest) => {
    const res = (await request.post('/kb', body)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  update: async (kbId: string, body: KbUpsertRequest) => {
    const res = (await request.put(`/kb/${kbId}`, body)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  remove: async (kbId: string) => {
    await request.delete(`/kb/${kbId}`)
  },
}

export const kbMemberApi = {
  list: async (kbId: string) => {
    const res = (await request.get(`/kb/${kbId}/members`)) as unknown as Result<KbMember[]>
    return res.data
  },

  add: async (kbId: string, body: KbMemberAddRequest) => {
    const res = (await request.post(`/kb/${kbId}/members`, body)) as unknown as Result<KbMember>
    return res.data
  },

  updateRole: async (kbId: string, memberId: string, role: KbRole) => {
    const res = (await request.put(`/kb/${kbId}/members/${memberId}`, { role })) as unknown as Result<KbMember>
    return res.data
  },

  remove: async (kbId: string, memberId: string) => {
    await request.delete(`/kb/${kbId}/members/${memberId}`)
  },
}

export interface KbSearchHit {
  content: string
  score: number
  kbId: string
  docId: string
  chunkId: string
  seq: number
  docName?: string
}

export const kbSearchApi = {
  search: async (kbId: string, q: string, topK = 5, threshold?: number) => {
    const res = (await request.get(`/kb/${kbId}/search`, {
      params: { q, topK, ...(threshold != null ? { threshold } : {}) },
    })) as unknown as Result<KbSearchHit[]>
    return res.data
  },
}
