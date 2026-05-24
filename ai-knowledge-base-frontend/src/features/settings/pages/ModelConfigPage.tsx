import { useState } from 'react'
import { Plus, RefreshCw, Settings, Star, Trash2 } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import request from '@/lib/request'
import type { Result } from '@/types/api'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { LightSidebar } from '@/components/layout/LightSidebar'

interface ModelConfig {
  id: string
  modelType: string
  provider: string
  modelName: string
  apiKeyMasked?: string
  baseUrl?: string
  isDefault: boolean
  enabled: boolean
  description?: string
}

const MODEL_KEYS = {
  all: ['system', 'model-configs'] as const,
}

function useModelConfigs() {
  return useQuery({
    queryKey: MODEL_KEYS.all,
    queryFn: async () => {
      const res = (await request.get('/system/model-configs')) as unknown as Result<ModelConfig[]>
      return res.data ?? []
    },
  })
}

interface FormState {
  id?: string
  modelType: string
  provider: string
  modelName: string
  apiKey: string
  baseUrl: string
  description: string
}

const initForm = (): FormState => ({
  modelType: 'CHAT',
  provider: 'DASHSCOPE',
  modelName: '',
  apiKey: '',
  baseUrl: '',
  description: '',
})

export function ModelConfigPage() {
  const qc = useQueryClient()
  const { data, isLoading } = useModelConfigs()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [form, setForm] = useState<FormState>(initForm())

  const saveMut = useMutation({
    mutationFn: (params: Record<string, unknown>) =>
      request.post('/system/model-configs', params),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: MODEL_KEYS.all })
      setDialogOpen(false)
    },
  })

  const toggleMut = useMutation({
    mutationFn: (id: string) => request.post(`/system/model-configs/${id}/toggle-enabled`),
    onSuccess: () => qc.invalidateQueries({ queryKey: MODEL_KEYS.all }),
  })

  const setDefaultMut = useMutation({
    mutationFn: (id: string) => request.post(`/system/model-configs/${id}/set-default`),
    onSuccess: () => qc.invalidateQueries({ queryKey: MODEL_KEYS.all }),
  })

  const deleteMut = useMutation({
    mutationFn: (id: string) => request.delete(`/system/model-configs/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: MODEL_KEYS.all }),
  })

  const openCreate = () => {
    setForm(initForm())
    setDialogOpen(true)
  }

  const openEdit = (c: ModelConfig) => {
    setForm({
      id: c.id,
      modelType: c.modelType,
      provider: c.provider,
      modelName: c.modelName,
      apiKey: '',
      baseUrl: c.baseUrl ?? '',
      description: c.description ?? '',
    })
    setDialogOpen(true)
  }

  const chatConfigs = data?.filter((c) => c.modelType === 'CHAT') ?? []
  const embeddingConfigs = data?.filter((c) => c.modelType === 'EMBEDDING') ?? []

  const ConfigGroup = ({ title, items }: { title: string; items: ModelConfig[] }) => (
    <div className="space-y-3">
      <h3 className="font-medium">{title}</h3>
      {items.length === 0 ? (
        <p className="text-sm text-muted-foreground">暂无配置</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {items.map((c) => (
            <Card key={c.id} className={`relative ${!c.enabled ? 'opacity-60' : ''}`}>
              <CardHeader className="pb-2 flex flex-row items-start justify-between space-y-0">
                <div>
                  <CardTitle className="text-sm">{c.modelName}</CardTitle>
                  <p className="text-xs text-muted-foreground mt-0.5">{c.provider}</p>
                </div>
                <div className="flex items-center gap-1">
                  {c.isDefault && <Badge variant="default" className="text-[10px] py-0">默认</Badge>}
                  <Badge variant={c.enabled ? 'secondary' : 'outline'} className="text-[10px] py-0">
                    {c.enabled ? '启用' : '禁用'}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="pt-0">
                {c.baseUrl && <p className="text-xs text-muted-foreground truncate">{c.baseUrl}</p>}
                {c.apiKeyMasked && <p className="text-xs text-muted-foreground mt-0.5">Key: {c.apiKeyMasked}</p>}
                {c.description && <p className="text-xs text-muted-foreground mt-0.5">{c.description}</p>}

                <div className="flex items-center gap-1 mt-3">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(c)} className="h-7 text-xs">
                    <Settings className="h-3 w-3 mr-1" />
                    编辑
                  </Button>
                  {!c.isDefault && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 text-xs"
                      onClick={() => setDefaultMut.mutate(c.id)}
                      disabled={setDefaultMut.isPending}
                    >
                      <Star className="h-3 w-3 mr-1" />
                      设为默认
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 text-xs"
                    onClick={() => toggleMut.mutate(c.id)}
                    disabled={toggleMut.isPending}
                  >
                    <RefreshCw className="h-3 w-3 mr-1" />
                    {c.enabled ? '禁用' : '启用'}
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 text-xs text-destructive hover:text-destructive"
                    onClick={() => { if (confirm(`确定删除「${c.modelName}」？`)) deleteMut.mutate(c.id) }}
                    disabled={deleteMut.isPending}
                  >
                    <Trash2 className="h-3 w-3 mr-1" />
                    删除
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )

  return (
    <div className="flex w-full h-[calc(100vh-2rem)] bg-gray-100 rounded-xl overflow-hidden">
      <LightSidebar />
      <div className="flex-1 p-10 bg-white dark:bg-zinc-950 rounded-2xl m-4 ml-0 shadow-sm border border-gray-200 overflow-auto">
        <div className="max-w-4xl space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold">模型配置</h1>
              <p className="text-sm text-muted-foreground mt-1">管理 LLM 和 Embedding 接入参数</p>
            </div>
            <Button onClick={openCreate}>
              <Plus className="h-4 w-4 mr-2" />
              新增配置
            </Button>
          </div>

          {isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-32" />
              <Skeleton className="h-32" />
            </div>
          ) : (
            <div className="space-y-8">
              <ConfigGroup title="对话模型（CHAT）" items={chatConfigs} />
              <ConfigGroup title="向量模型（EMBEDDING）" items={embeddingConfigs} />
            </div>
          )}
        </div>

        {/* 新增/编辑 Dialog */}
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogContent className="sm:max-w-lg">
            <DialogHeader>
              <DialogTitle>{form.id ? '编辑模型配置' : '新增模型配置'}</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 pt-2">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <Label>模型类型</Label>
                  <select
                    value={form.modelType}
                    onChange={(e) => setForm((f) => ({ ...f, modelType: e.target.value }))}
                    className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  >
                    <option value="CHAT">对话模型（CHAT）</option>
                    <option value="EMBEDDING">向量模型（EMBEDDING）</option>
                  </select>
                </div>
                <div className="space-y-1.5">
                  <Label>提供商</Label>
                  <select
                    value={form.provider}
                    onChange={(e) => setForm((f) => ({ ...f, provider: e.target.value }))}
                    className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  >
                    <option value="DASHSCOPE">通义百炼（DASHSCOPE）</option>
                    <option value="OPENAI">OpenAI</option>
                    <option value="OLLAMA">Ollama（本地）</option>
                  </select>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label>模型名称 <span className="text-destructive">*</span></Label>
                <Input
                  value={form.modelName}
                  onChange={(e) => setForm((f) => ({ ...f, modelName: e.target.value }))}
                  placeholder="如 qwen-plus / text-embedding-v3 / bge-m3"
                />
              </div>

              <div className="space-y-1.5">
                <Label>API Key {form.id && <span className="text-muted-foreground text-xs">（留空保留原值）</span>}</Label>
                <Input
                  type="password"
                  value={form.apiKey}
                  onChange={(e) => setForm((f) => ({ ...f, apiKey: e.target.value }))}
                  placeholder="sk-..."
                  autoComplete="off"
                />
              </div>

              <div className="space-y-1.5">
                <Label>Base URL</Label>
                <Input
                  value={form.baseUrl}
                  onChange={(e) => setForm((f) => ({ ...f, baseUrl: e.target.value }))}
                  placeholder="https://dashscope.aliyuncs.com/compatible-mode"
                />
              </div>

              <div className="space-y-1.5">
                <Label>备注</Label>
                <Input
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                  placeholder="说明用途或版本"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" onClick={() => setDialogOpen(false)}>取消</Button>
                <Button
                  disabled={saveMut.isPending || !form.modelName}
                  onClick={() => saveMut.mutate({ ...form })}
                >
                  {saveMut.isPending ? '保存中…' : '保存'}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  )
}
