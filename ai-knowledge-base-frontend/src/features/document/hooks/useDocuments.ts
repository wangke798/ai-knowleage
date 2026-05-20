import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { documentApi, type DocumentPageQuery } from '../api'

export function useDocuments(kbId: number, query: DocumentPageQuery = {}) {
  return useQuery({
    queryKey: ['documents', kbId, query],
    queryFn: () => documentApi.page(kbId, query),
    enabled: !!kbId,
    // 有 PENDING/PROCESSING 的轮询交给页面层面控制 refetchInterval（保留扩展点）
  })
}

export function useUploadDocument(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ file, onProgress }: { file: File; onProgress?: (p: number) => void }) =>
      documentApi.upload(kbId, file, onProgress),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', kbId] })
    },
  })
}

export function useDeleteDocument(kbId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (docId: number) => documentApi.remove(kbId, docId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', kbId] })
    },
  })
}
