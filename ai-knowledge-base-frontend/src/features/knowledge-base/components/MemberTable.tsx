import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select } from '@/components/ui/select'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import {
  useKbMembers,
  useRemoveKbMember,
  useUpdateKbMemberRole,
} from '../hooks/useKbMembers'
import type { KbMember, KbRole } from '@/types/kb'

interface MemberTableProps {
  kbId: number
  canManage: boolean
  onAddClick?: () => void
}

const roleLabel: Record<KbRole, string> = {
  OWNER: '所有者',
  EDITOR: '编辑者',
  VIEWER: '只读者',
}

export function MemberTable({ kbId, canManage, onAddClick }: MemberTableProps) {
  const { data, isLoading } = useKbMembers(kbId)
  const updateRole = useUpdateKbMemberRole(kbId)
  const removeMember = useRemoveKbMember(kbId)
  const [pendingDelete, setPendingDelete] = useState<KbMember | null>(null)

  if (isLoading) {
    return <div className="text-sm text-muted-foreground">加载中...</div>
  }

  const members = data ?? []

  return (
    <div className="space-y-3">
      {canManage && onAddClick && (
        <div className="flex justify-end">
          <Button size="sm" onClick={onAddClick}>添加成员</Button>
        </div>
      )}

      <div className="rounded-md border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-muted/40">
            <tr className="text-left">
              <th className="px-4 py-2 font-medium">用户</th>
              <th className="px-4 py-2 font-medium">用户名</th>
              <th className="px-4 py-2 font-medium">角色</th>
              {canManage && <th className="px-4 py-2 font-medium w-24">操作</th>}
            </tr>
          </thead>
          <tbody>
            {members.length === 0 ? (
              <tr>
                <td colSpan={canManage ? 4 : 3} className="px-4 py-6 text-center text-muted-foreground">
                  暂无成员
                </td>
              </tr>
            ) : (
              members.map((m) => {
                const isOwner = m.role === 'OWNER'
                const editable = canManage && !isOwner
                return (
                  <tr key={m.id} className="border-t">
                    <td className="px-4 py-2">{m.nickname ?? m.username ?? `#${m.userId}`}</td>
                    <td className="px-4 py-2 text-muted-foreground">{m.username ?? '-'}</td>
                    <td className="px-4 py-2">
                      {editable ? (
                        <Select
                          value={m.role}
                          onChange={(e) =>
                            updateRole.mutate({ memberId: m.id, role: e.target.value as KbRole })
                          }
                          disabled={updateRole.isPending}
                          className="h-8 max-w-[140px]"
                        >
                          <option value="EDITOR">编辑者</option>
                          <option value="VIEWER">只读者</option>
                        </Select>
                      ) : (
                        <Badge variant={isOwner ? 'default' : 'secondary'}>{roleLabel[m.role]}</Badge>
                      )}
                    </td>
                    {canManage && (
                      <td className="px-4 py-2">
                        {!isOwner && (
                          <Button
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setPendingDelete(m)}
                            aria-label="移除成员"
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        )}
                      </td>
                    )}
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      <ConfirmDialog
        open={!!pendingDelete}
        onOpenChange={(v) => !v && setPendingDelete(null)}
        title="移除成员？"
        description={`将「${pendingDelete?.nickname ?? pendingDelete?.username ?? pendingDelete?.userId}」从知识库中移除。`}
        confirmText="移除"
        destructive
        loading={removeMember.isPending}
        onConfirm={() => {
          if (!pendingDelete) return
          removeMember.mutate(pendingDelete.id, {
            onSuccess: () => setPendingDelete(null),
          })
        }}
      />
    </div>
  )
}
