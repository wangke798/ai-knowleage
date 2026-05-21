import request from '@/lib/request'
import type { PageResult, Result } from '@/types/api'
import type { KbDocument } from '@/types/document'

export interface DocumentPageQuery {
  page?: number
  size?: number
  keyword?: string
}

export const documentApi = {
  page: async (kbId: string, params: DocumentPageQuery = {}) => {
    const res = (await request.get(`/kb/${kbId}/documents`, { params })) as unknown as Result<
      PageResult<KbDocument>
    >
    return res.data
  },

  detail: async (kbId: string, docId: string) => {
    const res = (await request.get(`/kb/${kbId}/documents/${docId}`)) as unknown as Result<KbDocument>
    return res.data
  },

  /**
   * 上传文件。onProgress 在 0~100 之间。
   */
  upload: async (
    kbId: string,
    file: File,
    onProgress?: (percent: number) => void,
  ) => {
    const form = new FormData()
    form.append('file', file)
    const res = (await request.post(`/kb/${kbId}/documents`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) {
          onProgress(Math.round((e.loaded / e.total) * 100))
        }
      },
    })) as unknown as Result<KbDocument>
    return res.data
  },

  remove: async (kbId: string, docId: string) => {
    await request.delete(`/kb/${kbId}/documents/${docId}`)
  },

  reparse: async (kbId: string, docId: string) => {
    const res = (await request.post(
      `/kb/${kbId}/documents/${docId}/reparse`,
    )) as unknown as Result<KbDocument>
    return res.data
  },

  downloadUrl: (kbId: string, docId: string) => `/api/kb/${kbId}/documents/${docId}/download`,
}
