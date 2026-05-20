export type ParseStatus = 'PENDING' | 'PROCESSING' | 'DONE' | 'FAILED'

export interface KbDocument {
  id: number
  kbId: number
  name: string
  fileSize: number
  mimeType: string
  fileHash: string
  version: number
  parseStatus: ParseStatus
  parseError?: string
  chunkCount?: number
  uploaderId: number
  uploaderName?: string
  createTime: string
  updateTime: string
}
