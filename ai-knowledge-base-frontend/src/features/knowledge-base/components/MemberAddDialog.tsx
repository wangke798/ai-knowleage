import { useEffect, useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select } from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { useAddKbMember } from '../hooks/useKbMembers'
import type { KbRole } from '@/types/kb'

interface MemberAddDialogProps {
  open: boolean
  onOpenChange: (v: boolean) => void
  kbId: number
}

export function MemberAddDialog({ open, onOpenChange, kbId }: MemberAddDialogProps) {
  const [userId, setUserId] = useState('')
  const [role, setRole] = useState<KbRole>('VIEWER')
  const [error, setError] = useState<string | null>(null)
  const addMember = useAddKbMember(kbId)

  useEffect(() => {
    if (open) {
      setUserId('')
      setRole('VIEWER')
      setError(null)
    }
  }, [open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const parsed = Number(userId.trim())
    if (!Number.isInteger(parsed) || parsed <= 0) {
      setError('请输入有效的用户 ID')
      return
    }
    addMember.mutate(
      { userId: parsed, role },
      {
        onSuccess: () => onOpenChange(false),
        onError: (err: unknown) => {
          const message = (err as { message?: string })?.message ?? '添加失败'
          setError(message)
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>添加成员</DialogTitle>
            <DialogDescription>
              通过用户 ID 邀请成员（Phase 1 鉴权完成后会改为用户名搜索）。
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="member-userid">用户 ID *</Label>
              <Input
                id="member-userid"
                type="number"
                min={1}
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="输入用户 ID"
                autoFocus
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="member-role">角色</Label>
              <Select
                id="member-role"
                value={role}
                onChange={(e) => setRole(e.target.value as KbRole)}
              >
                <option value="VIEWER">只读者</option>
                <option value="EDITOR">编辑者</option>
              </Select>
            </div>
            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={addMember.isPending}>
              取消
            </Button>
            <Button type="submit" disabled={addMember.isPending}>
              {addMember.isPending ? '提交中...' : '添加'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
