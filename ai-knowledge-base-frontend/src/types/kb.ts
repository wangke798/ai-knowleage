export type KbRole = 'OWNER' | 'EDITOR' | 'VIEWER'

export interface KnowledgeBase {
  id: number
  name: string
  description?: string
  ownerId: number
  ownerName?: string
  embeddingModel?: string
  status: number
  /** 当前用户在该知识库中的角色 */
  currentUserRole?: KbRole
  createTime: string
  updateTime?: string
}

export interface KbMember {
  id: number
  kbId: number
  userId: number
  username?: string
  nickname?: string
  role: KbRole
}

export interface KbUpsertRequest {
  name: string
  description?: string
  embeddingModel?: string
}

export interface KbMemberAddRequest {
  userId: number
  role: KbRole
}
