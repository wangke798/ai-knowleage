import * as React from 'react'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

interface DialogContextValue {
  open: boolean
  setOpen: (v: boolean) => void
}
const DialogCtx = React.createContext<DialogContextValue | null>(null)

interface DialogProps {
  open?: boolean
  defaultOpen?: boolean
  onOpenChange?: (open: boolean) => void
  children: React.ReactNode
}

export function Dialog({ open, defaultOpen, onOpenChange, children }: DialogProps) {
  const [internalOpen, setInternalOpen] = React.useState(defaultOpen ?? false)
  const isControlled = open !== undefined
  const value = isControlled ? open! : internalOpen
  const setOpen = (v: boolean) => {
    if (!isControlled) setInternalOpen(v)
    onOpenChange?.(v)
  }
  return <DialogCtx.Provider value={{ open: value, setOpen }}>{children}</DialogCtx.Provider>
}

interface DialogTriggerProps {
  asChild?: boolean
  children: React.ReactElement
}

export function DialogTrigger({ children }: DialogTriggerProps) {
  const ctx = React.useContext(DialogCtx)!
  return React.cloneElement(children, {
    onClick: (e: React.MouseEvent) => {
      ;(children.props as { onClick?: (e: React.MouseEvent) => void }).onClick?.(e)
      ctx.setOpen(true)
    },
  } as React.HTMLAttributes<HTMLElement>)
}

export function DialogContent({ className, children }: { className?: string; children: React.ReactNode }) {
  const ctx = React.useContext(DialogCtx)!
  React.useEffect(() => {
    if (!ctx.open) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && ctx.setOpen(false)
    document.addEventListener('keydown', onKey)
    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = prevOverflow
    }
  }, [ctx.open, ctx])

  if (!ctx.open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 animate-in fade-in" onClick={() => ctx.setOpen(false)} />
      <div
        role="dialog"
        className={cn(
          'relative z-10 w-full max-w-lg rounded-lg border bg-background p-6 shadow-lg',
          className,
        )}
      >
        <button
          type="button"
          onClick={() => ctx.setOpen(false)}
          className="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-background transition-opacity hover:opacity-100"
          aria-label="关闭"
        >
          <X className="h-4 w-4" />
        </button>
        {children}
      </div>
    </div>
  )
}

export function DialogHeader({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('flex flex-col space-y-1.5 text-left mb-4', className)} {...props} />
}

export function DialogTitle({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) {
  return <h2 className={cn('text-lg font-semibold leading-none tracking-tight', className)} {...props} />
}

export function DialogDescription({ className, ...props }: React.HTMLAttributes<HTMLParagraphElement>) {
  return <p className={cn('text-sm text-muted-foreground', className)} {...props} />
}

export function DialogFooter({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('mt-6 flex justify-end gap-2', className)} {...props} />
}

export function useDialog() {
  const ctx = React.useContext(DialogCtx)
  if (!ctx) throw new Error('useDialog must be used within <Dialog>')
  return ctx
}
