import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { useKnowledgeBase } from '../hooks/useKnowledgeBases'
import { KbUpsertDialog } from '../components/KbUpsertDialog'
import { MemberTable } from '../components/MemberTable'
import { MemberAddDialog } from '../components/MemberAddDialog'
import { DocumentUploader } from '@/features/document/components/DocumentUploader'
import { DocumentTable } from '@/features/document/components/DocumentTable'
import { KbSearchPanel } from '../components/KbSearchPanel'
import type { KbRole } from '@/types/kb'

const roleLabel: Record<KbRole, string> = {
  OWNER: '所有者',
  EDITOR: '编辑者',
  VIEWER: '只读者',
}

export function KbDetailPage() {
  const { kbId } = useParams()
  const id = kbId
  const { data: kb, isLoading, isError, error } = useKnowledgeBase(id)
  const [editOpen, setEditOpen] = useState(false)
  const [addMemberOpen, setAddMemberOpen] = useState(false)

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-32" />
      </div>
    )
  }

  if (isError || !kb) {
    return (
      <EmptyState
        title="无法加载知识库"
        description={(error as { message?: string })?.message ?? '可能已被删除，或您没有权限访问'}
        action={
          <Link to="/kb">
            <Button variant="outline">返回列表</Button>
          </Link>
        }
      />
    )
  }

  const role = kb.currentUserRole
  const canEdit = role === 'OWNER' || role === 'EDITOR'
  const isOwner = role === 'OWNER'

  return (
    <div className="space-y-6">
      <div>
        <Link to="/kb" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground mb-2">
          <ArrowLeft className="mr-1 h-3.5 w-3.5" />
          知识库列表
        </Link>
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold truncate">{kb.name}</h1>
              {role && <Badge variant={isOwner ? 'default' : 'secondary'}>{roleLabel[role]}</Badge>}
            </div>
            {kb.description && <p className="mt-1 text-sm text-muted-foreground">{kb.description}</p>}
            <div className="mt-2 flex flex-wrap gap-3 text-xs text-muted-foreground">
              {kb.ownerName && <span>所有者：{kb.ownerName}</span>}
              {kb.embeddingModel && <span>Embedding：{kb.embeddingModel}</span>}
            </div>
          </div>
          {canEdit && (
            <Button variant="outline" onClick={() => setEditOpen(true)}>
              <Pencil className="h-4 w-4" />
              编辑
            </Button>
          )}
        </div>
      </div>

      <Tabs defaultValue="documents">
        <TabsList>
          <TabsTrigger value="documents">文档管理</TabsTrigger>
          <TabsTrigger value="search">检索调试</TabsTrigger>
          <TabsTrigger value="members">成员管理</TabsTrigger>
          <TabsTrigger value="settings">基本设置</TabsTrigger>
        </TabsList>

        <TabsContent value="documents">
          <div className="space-y-6">
            {canEdit ? (
              <DocumentUploader kbId={kb.id} />
            ) : (
              <EmptyState
                title="无写入权限"
                description="仅所有者 / 编辑者可上传文档，您可以查看与下载已有文档"
              />
            )}
            <DocumentTable kbId={kb.id} canWrite={canEdit} />
          </div>
        </TabsContent>

        <TabsContent value="search">
          <KbSearchPanel kbId={kb.id} />
        </TabsContent>

        <TabsContent value="members">
          <MemberTable
            kbId={kb.id}
            canManage={isOwner}
            onAddClick={() => setAddMemberOpen(true)}
          />
        </TabsContent>

        <TabsContent value="settings">
          <div className="rounded-md border p-6 space-y-4">
            <div>
              <div className="text-sm text-muted-foreground">名称</div>
              <div className="mt-1">{kb.name}</div>
            </div>
            <div>
              <div className="text-sm text-muted-foreground">描述</div>
              <div className="mt-1">{kb.description || '-'}</div>
            </div>
            <div>
              <div className="text-sm text-muted-foreground">Embedding 模型</div>
              <div className="mt-1">{kb.embeddingModel || '（系统默认）'}</div>
            </div>
            {canEdit && (
              <Button variant="outline" onClick={() => setEditOpen(true)}>
                <Pencil className="h-4 w-4" />
                修改信息
              </Button>
            )}
          </div>
        </TabsContent>
      </Tabs>

      <KbUpsertDialog open={editOpen} onOpenChange={setEditOpen} initial={kb} />
      <MemberAddDialog open={addMemberOpen} onOpenChange={setAddMemberOpen} kbId={kb.id} />
    </div>
  )
}
