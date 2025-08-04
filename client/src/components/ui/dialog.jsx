"use client"

import * as React from "react"
import * as DialogPrimitive from "@radix-ui/react-dialog"
import { XIcon } from "lucide-react"
import { cn } from "@/lib/utils"

function Dialog(props) {
  return <DialogPrimitive.Root data-slot="dialog" {...props} />
}

function DialogTrigger(props) {
  return <DialogPrimitive.Trigger data-slot="dialog-trigger" {...props} />
}

function DialogPortal(props) {
  return <DialogPrimitive.Portal data-slot="dialog-portal" {...props} />
}

function DialogClose(props) {
  return <DialogPrimitive.Close data-slot="dialog-close" {...props} />
}

function DialogOverlay({ className, ...props }) {
  return (
      <DialogPrimitive.Overlay
          data-slot="dialog-overlay"
          className={cn(
              "fixed inset-0 z-50 bg-black/50 backdrop-blur-sm",
              "data-[state=open]:animate-in data-[state=closed]:animate-out",
              "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
              className
          )}
          {...props}
      />
  )
}

function DialogContent({ className, children, showCloseButton = true, ...props }) {
  return (
      <DialogPortal>
        <DialogOverlay />
        <DialogPrimitive.Content
            data-slot="dialog-content"
            className={cn(
                // Positioning
                "fixed top-1/2 left-1/2 z-50 translate-x-[-50%] translate-y-[-50%]",
                // Size
                "w-full max-w-[calc(100%-2rem)] sm:max-w-lg",
                // Layout + Styling
                "grid gap-4 rounded-[var(--radius)] border border-[var(--color-border)] bg-[var(--color-popover)] p-6 shadow-lg font-ui text-sm",
                // Focus Ring
                "ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2",
                // Animations
                "data-[state=open]:animate-in data-[state=closed]:animate-out",
                "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
                "data-[state=open]:zoom-in-95 data-[state=closed]:zoom-out-95",
                className
            )}
            {...props}
        >
          {children}
          {showCloseButton && (
              <DialogPrimitive.Close
                  data-slot="dialog-close"
                  className="absolute top-4 right-4 rounded-[calc(var(--radius)-2px)] p-1 text-[var(--color-muted-foreground)] transition-opacity hover:opacity-100 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2 disabled:pointer-events-none"
              >
                <XIcon className="size-4" />
                <span className="sr-only">Close</span>
              </DialogPrimitive.Close>
          )}
        </DialogPrimitive.Content>
      </DialogPortal>
  )
}

function DialogHeader({ className, ...props }) {
  return (
      <div
          data-slot="dialog-header"
          className={cn("flex flex-col gap-2 text-center sm:text-left", className)}
          {...props}
      />
  )
}

function DialogFooter({ className, ...props }) {
  return (
      <div
          data-slot="dialog-footer"
          className={cn("flex flex-col-reverse gap-2 sm:flex-row sm:justify-end", className)}
          {...props}
      />
  )
}

function DialogTitle({ className, ...props }) {
  return (
      <DialogPrimitive.Title
          data-slot="dialog-title"
          className={cn("text-lg font-semibold font-ui text-[var(--color-foreground)]", className)}
          {...props}
      />
  )
}

function DialogDescription({ className, ...props }) {
  return (
      <DialogPrimitive.Description
          data-slot="dialog-description"
          className={cn("text-[var(--color-muted-foreground)] text-sm font-content", className)}
          {...props}
      />
  )
}

export {
  Dialog,
  DialogTrigger,
  DialogPortal,
  DialogOverlay,
  DialogContent,
  DialogClose,
  DialogHeader,
  DialogFooter,
  DialogTitle,
  DialogDescription,
}
