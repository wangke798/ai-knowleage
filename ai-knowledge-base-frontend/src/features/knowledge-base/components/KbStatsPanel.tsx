import { BarChart3, BookOpen, Layers, MessageSquare, TrendingUp } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useKbStats } from '../hooks/useKnowledgeBases'

interface Props {
  kbId: string
}

interface StatCardProps {
  icon: React.ElementType
  label: string
  value: number | string
  sub?: string
  color?: string
}

function StatCard({ icon: Icon, label, value, sub, color = 'text-primary' }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
        <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
        <Icon className={`h-4 w-4 ${color}`} />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {sub && <p className="text-xs text-muted-foreground mt-1">{sub}</p>}
      </CardContent>
    </Card>
  )
}

export function KbStatsPanel({ kbId }: Props) {
  const { data, isLoading } = useKbStats(kbId)

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <Card key={i}>
            <CardHeader className="pb-2">
              <Skeleton className="h-4 w-20" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16" />
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  if (!data) return null

  const parseRate = data.docCount > 0
    ? Math.round((data.parsedDocCount / data.docCount) * 100)
    : 0

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <StatCard
          icon={BookOpen}
          label="文档总数"
          value={data.docCount}
          sub={`已解析 ${data.parsedDocCount} 篇`}
          color="text-blue-500"
        />
        <StatCard
          icon={TrendingUp}
          label="解析完成率"
          value={`${parseRate}%`}
          sub={`${data.parsedDocCount} / ${data.docCount}`}
          color="text-green-500"
        />
        <StatCard
          icon={Layers}
          label="文本分块数"
          value={data.chunkCount.toLocaleString()}
          sub="向量库存储"
          color="text-purple-500"
        />
        <StatCard
          icon={MessageSquare}
          label="会话总数"
          value={data.conversationCount}
          sub="历史对话"
          color="text-orange-500"
        />
        <StatCard
          icon={BarChart3}
          label="消息总数"
          value={data.messageCount.toLocaleString()}
          sub="含用户 + AI"
          color="text-pink-500"
        />
      </div>

      {/* 简单进度条：解析进度 */}
      <div className="space-y-1.5">
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">解析进度</span>
          <span className="font-medium">{parseRate}%</span>
        </div>
        <div className="h-2 w-full rounded-full bg-muted overflow-hidden">
          <div
            className="h-full rounded-full bg-primary transition-all"
            style={{ width: `${parseRate}%` }}
          />
        </div>
        <p className="text-xs text-muted-foreground">
          {data.parsedDocCount} 篇已完成 · {data.docCount - data.parsedDocCount} 篇待处理
        </p>
      </div>
    </div>
  )
}
