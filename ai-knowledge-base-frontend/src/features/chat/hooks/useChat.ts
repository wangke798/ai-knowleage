import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { chatApi, type ConversationCreateRequest } from '../api'

export const conversationKeys = {
  all: ['conversations'] as const,
  list: () => [...conversationKeys.all, 'list'] as const,
  detail: (id: string) => [...conversationKeys.all, 'detail', id] as const,
  messages: (id: string) => [...conversationKeys.all, id, 'messages'] as const,
}

export function useConversations() {
  return useQuery({
    queryKey: conversationKeys.list(),
    queryFn: () => chatApi.listConversations(),
  })
}

export function useConversation(id: string | undefined) {
  return useQuery({
    queryKey: conversationKeys.detail(id ?? ''),
    queryFn: () => chatApi.detailConversation(id as string),
    enabled: !!id,
  })
}

export function useMessages(id: string | undefined) {
  return useQuery({
    queryKey: conversationKeys.messages(id ?? ''),
    queryFn: () => chatApi.listMessages(id as string),
    enabled: !!id,
  })
}

export function useCreateConversation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: ConversationCreateRequest) => chatApi.createConversation(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: conversationKeys.list() })
    },
  })
}

export function useRenameConversation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: string; title: string }) => chatApi.renameConversation(vars.id, vars.title),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: conversationKeys.list() })
      qc.invalidateQueries({ queryKey: conversationKeys.detail(vars.id) })
    },
  })
}

export function useDeleteConversation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => chatApi.deleteConversation(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: conversationKeys.list() })
    },
  })
}
