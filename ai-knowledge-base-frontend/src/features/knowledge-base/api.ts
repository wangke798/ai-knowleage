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

  detail: async (kbId: number) => {
    const res = (await request.get(`/kb/${kbId}`)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  create: async (body: KbUpsertRequest) => {
    const res = (await request.post('/kb', body)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  update: async (kbId: number, body: KbUpsertRequest) => {
    const res = (await request.put(`/kb/${kbId}`, body)) as unknown as Result<KnowledgeBase>
    return res.data
  },

  remove: async (kbId: number) => {
    await request.delete(`/kb/${kbId}`)
  },
}

export const kbMemberApi = {
  list: async (kbId: number) => {
    const res = (await request.get(`/kb/${kbId}/members`)) as unknown as Result<KbMember[]>
    return res.data
  },

  add: async (kbId: number, body: KbMemberAddRequest) => {
    const res = (await request.post(`/kb/${kbId}/members`, body)) as unknown as Result<KbMember>
    return res.data
  },

  updateRole: async (kbId: number, memberId: number, role: KbRole) => {
    const res = (await request.put(`/kb/${kbId}/members/${memberId}`, { role })) as unknown as Result<KbMember>
    return res.data
  },

  remove: async (kbId: number, memberId: number) => {
    await request.delete(`/kb/${kbId}/members/${memberId}`)
  },
}
