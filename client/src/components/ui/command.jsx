"use client"

import * as React from "react"
import { Command as CommandPrimitive } from "cmdk"
import { SearchIcon } from "lucide-react"
import { cn } from "@/lib/utils"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

function Command({ className, ...props }) {
  return (
      <CommandPrimitive
          data-slot="command"
          className={cn(
              "bg-[var(--color-popover)] text-[var(--color-popover-foreground)] flex h-full w-full flex-col overflow-hidden rounded-[var(--radius)]",
              className
          )}
          {...props}
      />
  )
}

function CommandDialog({
  title = "Command Palette",
  description = "Search for a command to run...",
  children,
  className,
  showCloseButton = false,
  ...props
}) {
  return (
      <Dialog {...props}>
        <DialogHeader className="sr-only">
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <DialogContent
            className={cn(
                "fixed top-[25%] left-1/2 translate-x-[-50%] translate-y-[-45%]",
                "overflow-hidden p-0",
                className
            )}
            showCloseButton={showCloseButton}
        >
          <Command
              className="text-sm font-ui [&_[cmdk-group-heading]]:text-[var(--color-muted-foreground)] [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group]]:px-2 [&_[cmdk-group]:not([hidden])_~[cmdk-group]]:pt-0 [&_[cmdk-input-wrapper]_svg]:size-4 [&_[cmdk-input]]:h-10 [&_[cmdk-input]]:text-sm"
          >
            {children}
          </Command>
        </DialogContent>
      </Dialog>
  )
}


function CommandInput({ className, ...props }) {
  return (
      <div
          data-slot="command-input-wrapper"
          className="flex h-10 items-center gap-2 border-b border-[var(--color-border)] px-3 bg-[var(--color-input)]"
      >
        <SearchIcon className="size-4 shrink-0 opacity-50" />
        <CommandPrimitive.Input
            data-slot="command-input"
            className={cn(
                "placeholder:text-[var(--color-muted-foreground)] font-ui w-full bg-transparent text-sm outline-none",
                className
            )}
            {...props}
        />
      </div>
  )
}

function CommandList({ className, ...props }) {
  return (
      <CommandPrimitive.List
          data-slot="command-list"
          className={cn("max-h-[300px] scroll-py-1 overflow-x-hidden overflow-y-auto", className)}
          {...props}
      />
  )
}

function CommandEmpty(props) {
  return (
      <CommandPrimitive.Empty
          data-slot="command-empty"
          className="py-6 text-center text-sm text-[var(--color-muted-foreground)]"
          {...props}
      />
  )
}

function CommandGroup({ className, ...props }) {
  return (
      <CommandPrimitive.Group
          data-slot="command-group"
          className={cn(
              "text-[var(--color-foreground)] overflow-hidden p-1",
              "[&_[cmdk-group-heading]]:text-[var(--color-muted-foreground)] [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium",
              className
          )}
          {...props}
      />
  )
}

function CommandSeparator({ className, ...props }) {
  return (
      <CommandPrimitive.Separator
          data-slot="command-separator"
          className={cn("bg-[var(--color-border)] mx-1 h-px opacity-50", className)}
          {...props}
      />
  )
}

function CommandItem({ className, ...props }) {
  return (
      <CommandPrimitive.Item
          data-slot="command-item"
          className={cn(
              "relative flex cursor-default select-none items-center gap-2 rounded-[var(--radius)] px-2 py-2 text-sm font-ui text-[var(--color-foreground)]",
              "data-[selected=true]:bg-[var(--color-accent)] data-[selected=true]:text-[var(--color-accent-foreground)]",
              "[&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 text-[var(--color-muted-foreground)]",
              "data-[disabled=true]:pointer-events-none data-[disabled=true]:opacity-50",
              className
          )}
          {...props}
      />
  )
}

function CommandShortcut({ className, ...props }) {
  return (
      <span
          data-slot="command-shortcut"
          className={cn("ml-auto text-xs tracking-widest text-[var(--color-muted-foreground)]", className)}
          {...props}
      />
  )
}

export {
  Command,
  CommandDialog,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
  CommandShortcut,
  CommandSeparator,
}