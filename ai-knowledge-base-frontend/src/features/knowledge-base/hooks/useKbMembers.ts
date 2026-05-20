import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { kbMemberApi } from '../api'
import type { KbMemberAddRequest, KbRole } from '@/types/kb'

const MEMBER_KEYS = {
  list: (kbId: number) => ['kb', 'members', kbId] as const,
}

export function useKbMembers(kbId: number | undefined) {
  return useQuery({
    queryKey: MEMBER_KEYS.list(kbId ?? -1),
    queryFn: () => kbMemberApi.list(kbId!),
    enabled: kbId != null && kbId > 0,
  })
}

export function useAddKbMember(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: KbMemberAddRequest) => kbMemberApi.add(kbId, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}

export function useUpdateKbMemberRole(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ memberId, role }: { memberId: number; role: KbRole }) =>
      kbMemberApi.updateRole(kbId, memberId, role),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}

export function useRemoveKbMember(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (memberId: number) => kbMemberApi.remove(kbId, memberId),
    onSuccess: () => qc.invalidateQueries({ queryKey: MEMBER_KEYS.list(kbId) }),
  })
}
