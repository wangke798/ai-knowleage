export type ParseStatus = 'PENDING' | 'PROCESSING' | 'DONE' | 'FAILED'

export interface KbDocument {
  id: string
  kbId: string
  name: string
  fileSize: number
  mimeType: string
  fileHash: string
  version: number
  parseStatus: ParseStatus
  parseError?: string
  chunkCount?: number
  uploaderId: string
  uploaderName?: string
  createTime: string
  updateTime: string
}
