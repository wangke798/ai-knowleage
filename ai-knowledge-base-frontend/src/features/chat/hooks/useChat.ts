import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { chatApi, type ConversationCreateRequest, type ConversationListQuery } from '../api'

export const conversationKeys = {
  all: ['conversations'] as const,
  list: (params?: ConversationListQuery) => [...conversationKeys.all, 'list', params] as const,
  detail: (id: string) => [...conversationKeys.all, 'detail', id] as const,
  messages: (id: string) => [...conversationKeys.all, id, 'messages'] as const,
}

export function useConversations(params?: ConversationListQuery) {
  return useQuery({
    queryKey: conversationKeys.list(params),
    queryFn: () => chatApi.listConversations(params),
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
      qc.invalidateQueries({ queryKey: conversationKeys.all })
    },
  })
}

export function useRenameConversation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: string; title: string }) => chatApi.renameConversation(vars.id, vars.title),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: conversationKeys.all })
      qc.invalidateQueries({ queryKey: conversationKeys.detail(vars.id) })
    },
  })
}

export function useDeleteConversation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => chatApi.deleteConversation(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: conversationKeys.all })
    },
  })
}

export function useToggleFavorite() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => chatApi.toggleFavorite(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: conversationKeys.all })
    },
  })
}
