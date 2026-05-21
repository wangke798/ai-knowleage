import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { documentApi, type DocumentPageQuery } from '../api'
import type { PageResult } from '@/types/api'
import type { KbDocument } from '@/types/document'

export function useDocuments(kbId: string, query: DocumentPageQuery = {}) {
  return useQuery({
    queryKey: ['documents', kbId, query],
    queryFn: () => documentApi.page(kbId, query),
    enabled: !!kbId,
    // 当列表里仍有 PENDING/PROCESSING 时，每 2.5s 轮询刷新一次
    refetchInterval: (q) => {
      const data = q.state.data as PageResult<KbDocument> | undefined
      const hasRunning = data?.records?.some(
        (d) => d.parseStatus === 'PENDING' || d.parseStatus === 'PROCESSING',
      )
      return hasRunning ? 2500 : false
    },
  })
}

export function useUploadDocument(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ file, onProgress }: { file: File; onProgress?: (p: number) => void }) =>
      documentApi.upload(kbId, file, onProgress),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', kbId] })
    },
  })
}

export function useDeleteDocument(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (docId: string) => documentApi.remove(kbId, docId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', kbId] })
    },
  })
}

export function useReparseDocument(kbId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (docId: string) => documentApi.reparse(kbId, docId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', kbId] })
    },
  })
}
