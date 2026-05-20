import { useParams } from 'react-router-dom'

export function ChatPage() {
  const { conversationId } = useParams()
  return (
    <div className="flex h-full gap-4">
      {/* 左栏：历史会话列表 */}
      <aside className="w-60 border-r pr-4">
        <h2 className="font-semibold mb-3 text-sm">历史会话</h2>
        {/* TODO: Phase 4 实现 ConversationList */}
      </aside>
      {/* 右栏：聊天区域 */}
      <div className="flex-1 flex flex-col">
        <p className="text-muted-foreground text-sm">对话 #{conversationId} 开发中...</p>
        {/* TODO: Phase 4 实现 MessageBubble 列表 + ChatInput */}
      </div>
    </div>
  )
}
