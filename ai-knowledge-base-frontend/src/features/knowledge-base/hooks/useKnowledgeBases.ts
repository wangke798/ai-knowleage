import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { kbApi, type KbPageQuery } from '../api'
import type { KbUpsertRequest } from '@/types/kb'

const KB_KEYS = {
  all: ['kb'] as const,
  list: (q: KbPageQuery) => ['kb', 'list', q] as const,
  detail: (id: number) => ['kb', 'detail', id] as const,
}

export function useKnowledgeBases(query: KbPageQuery = {}) {
  return useQuery({
    queryKey: KB_KEYS.list(query),
    queryFn: () => kbApi.page(query),
  })
}

export function useKnowledgeBase(kbId: number | undefined) {
  return useQuery({
    queryKey: KB_KEYS.detail(kbId ?? -1),
    queryFn: () => kbApi.detail(kbId!),
    enabled: kbId != null && kbId > 0,
  })
}

export function useCreateKb() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: KbUpsertRequest) => kbApi.create(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: KB_KEYS.all })
    },
  })
}

export function useUpdateKb(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: KbUpsertRequest) => kbApi.update(kbId, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: KB_KEYS.all })
      qc.invalidateQueries({ queryKey: KB_KEYS.detail(kbId) })
    },
  })
}

export function useDeleteKb() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (kbId: number) => kbApi.remove(kbId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: KB_KEYS.all })
    },
  })
}
