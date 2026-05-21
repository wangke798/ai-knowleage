import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { kbMemberApi } from '../api'
import type { KbMemberAddRequest, KbRole } from '@/types/kb'

const MEMBER_KEYS = {
  list: (kbId: string) => ['kb', 'members', kbId] as const,
}

export function useKbMembers(kbId: string | undefined) {
  return useQuery({
    queryKey: MEMBER_KEYS.list(kbId ?? ''),
    queryFn: () => kbMemberApi.list(kbId!),
    enabled: !!kbId,
  })
}

export function useAddKbMember(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: KbMemberAddRequest) => kbMemberApi.add(kbId, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}

export function useUpdateKbMemberRole(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ memberId, role }: { memberId: string; role: KbRole }) =>
      kbMemberApi.updateRole(kbId, memberId, role),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}

export function useRemoveKbMember(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (memberId: string) => kbMemberApi.remove(kbId, memberId),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}
