import { useRef, type KeyboardEvent } from 'react'
import { Loader2, Send, Square } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'

interface Props {
  value: string
  onChange: (v: string) => void
  onSend: () => void
  onStop?: () => void
  isStreaming?: boolean
  disabled?: boolean
  placeholder?: string
}

export function ChatInput({ value, onChange, onSend, onStop, isStreaming, disabled, placeholder }: Props) {
  const ref = useRef<HTMLTextAreaElement>(null)

  const handleKey = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    // 回车发送，Shift+回车换行
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault()
      if (!isStreaming && value.trim()) onSend()
    }
  }

  return (
    <div className="border rounded-lg bg-background p-2 flex items-end gap-2">
      <Textarea
        ref={ref}
        rows={1}
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={handleKey}
        placeholder={placeholder ?? '输入问题，Shift+Enter 换行'}
        className="min-h-[40px] max-h-40 resize-none border-0 shadow-none focus-visible:ring-0"
      />
      {isStreaming ? (
        <Button type="button" variant="outline" onClick={onStop} title="停止生成">
          <Square className="h-4 w-4" />
        </Button>
      ) : (
        <Button
          type="button"
          onClick={onSend}
          disabled={disabled || !value.trim()}
          title="发送（Enter）"
        >
          {disabled ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
        </Button>
      )}
    </div>
  )
}
