import { useState } from 'react'
import { MessageSquarePlus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ConversationList } from '../components/ConversationList'
import { NewConversationDialog } from '../components/NewConversationDialog'

export function ChatHomePage() {
  const [open, setOpen] = useState(false)

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-4">
      <aside className="w-64 border-r pr-4 hidden md:block">
        <ConversationList onPickNew={() => setOpen(true)} />
      </aside>

      <main className="flex-1 flex flex-col items-center justify-center text-center gap-4">
        <MessageSquarePlus className="h-12 w-12 text-muted-foreground" />
        <div>
          <h1 className="text-xl font-semibold">开始与你的知识库对话</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            选择一个知识库，提出问题，系统会基于已上传的文档给出可溯源的回答。
          </p>
        </div>
        <Button size="lg" onClick={() => setOpen(true)}>
          <MessageSquarePlus className="h-4 w-4" />
          新建对话
        </Button>
      </main>

      <NewConversationDialog open={open} onOpenChange={setOpen} />
    </div>
  )
}
