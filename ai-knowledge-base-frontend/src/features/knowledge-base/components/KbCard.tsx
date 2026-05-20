import { useNavigate } from 'react-router-dom'
import { MoreVertical, Pencil, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { useDeleteKb } from '../hooks/useKnowledgeBases'
import type { KbRole, KnowledgeBase } from '@/types/kb'

interface KbCardProps {
  kb: KnowledgeBase
  onEdit: (kb: KnowledgeBase) => void
}

const roleLabel: Record<KbRole, string> = {
  OWNER: '所有者',
  EDITOR: '编辑者',
  VIEWER: '只读者',
}

const roleVariant: Record<KbRole, 'default' | 'secondary' | 'outline'> = {
  OWNER: 'default',
  EDITOR: 'secondary',
  VIEWER: 'outline',
}

export function KbCard({ kb, onEdit }: KbCardProps) {
  const navigate = useNavigate()
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const deleteKb = useDeleteKb()

  const role = kb.currentUserRole
  const canEdit = role === 'OWNER' || role === 'EDITOR'
  const canDelete = role === 'OWNER'

  return (
    <>
      <Card
        className="group cursor-pointer hover:border-primary/40 hover:shadow-md transition-all"
        onClick={() => navigate(`/kb/${kb.id}`)}
      >
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0 flex-1">
              <CardTitle className="truncate">{kb.name}</CardTitle>
              {kb.description && (
                <CardDescription className="mt-1 line-clamp-2">{kb.description}</CardDescription>
              )}
            </div>

            {(canEdit || canDelete) && (
              <div className="relative shrink-0" onClick={(e) => e.stopPropagation()}>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  onClick={() => setMenuOpen((v) => !v)}
                  aria-label="更多操作"
                >
                  <MoreVertical className="h-4 w-4" />
                </Button>
                {menuOpen && (
                  <>
                    <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
                    <div className="absolute right-0 top-full z-20 mt-1 min-w-[140px] rounded-md border bg-popover p-1 shadow-md">
                      {canEdit && (
                        <button
                          className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-sm hover:bg-muted"
                          onClick={() => {
                            setMenuOpen(false)
                            onEdit(kb)
                          }}
                        >
                          <Pencil className="h-3.5 w-3.5" />
                          编辑
                        </button>
                      )}
                      {canDelete && (
                        <button
                          className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-destructive hover:bg-destructive/10"
                          onClick={() => {
                            setMenuOpen(false)
                            setConfirmOpen(true)
                          }}
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                          删除
                        </button>
                      )}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent className="flex items-center justify-between pt-0">
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            {kb.ownerName && <span>所有者：{kb.ownerName}</span>}
          </div>
          {role && <Badge variant={roleVariant[role]}>{roleLabel[role]}</Badge>}
        </CardContent>
      </Card>

      <ConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title="删除知识库？"
        description={`将永久删除「${kb.name}」及其全部成员关系，文档与对话数据另行处理。该操作不可撤销。`}
        confirmText="删除"
        destructive
        loading={deleteKb.isPending}
        onConfirm={() =>
          deleteKb.mutate(kb.id, {
            onSuccess: () => setConfirmOpen(false),
          })
        }
      />
    </>
  )
}
