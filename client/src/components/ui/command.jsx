"use client"

import React from "react"
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
              "bg-[var(--color-popover)] text-[var(--color-popover-foreground)] flex h-full w-full flex-col overflow-hidden rounded-[var(--radius)] border border-[var(--color-border)] shadow-lg",
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
  showCloseButton = true,
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
                "overflow-hidden p-0 border-0 shadow-2xl w-[95vw] max-w-[700px] max-h-[80vh]",
                "fixed top-24 left-1/2 -translate-x-1/2", // << this positions it higher
                className
            )}
            showCloseButton={showCloseButton}
        >
          <Command className="[&_[cmdk-group-heading]]:text-[var(--color-muted-foreground)] [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group]]:px-2 [&_[cmdk-group]:not([hidden])_~[cmdk-group]]:pt-0 [&_[cmdk-input-wrapper]_svg]:h-4 [&_[cmdk-input-wrapper]_svg]:w-4 [&_[cmdk-input]]:h-11 [&_[cmdk-item]]:px-2 [&_[cmdk-item]]:py-2.5 [&_[cmdk-item]_svg]:h-4 [&_[cmdk-item]_svg]:w-4">
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
          className="flex h-11 items-center gap-2 border-b border-[var(--color-border)] bg-[var(--color-background)] px-3"
      >
        <SearchIcon className="size-4 shrink-0 text-[var(--color-muted-foreground)] opacity-60" />
        <CommandPrimitive.Input
            data-slot="command-input"
            className={cn(
                "w-full bg-transparent text-sm font-ui text-[var(--color-foreground)] placeholder:text-[var(--color-muted-foreground)] placeholder:opacity-60 outline-none",
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
          className={cn(
              "max-h-[500px] overflow-y-auto scroll-py-2 scrollbar-thin scrollbar-thumb-[var(--color-muted)] scrollbar-track-transparent",
              className
          )}
          {...props}
      />
  )
}

function CommandEmpty({ className, children = "No results found.", ...props }) {
  return (
      <CommandPrimitive.Empty
          data-slot="command-empty"
          className={cn(
              "py-6 text-center text-sm text-[var(--color-muted-foreground)] opacity-60",
              className
          )}
          {...props}
      >
        {children}
      </CommandPrimitive.Empty>
  )
}

function CommandGroup({ className, ...props }) {
  return (
      <CommandPrimitive.Group
          data-slot="command-group"
          className={cn(
              "text-[var(--color-foreground)] overflow-hidden p-1 [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-[var(--color-muted-foreground)]",
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
          className={cn("bg-[var(--color-border)] -mx-1 h-px", className)}
          {...props}
      />
  )
}

function CommandItem({ className, ...props }) {
  return (
      <CommandPrimitive.Item
          data-slot="command-item"
          className={cn(
              "relative flex select-none items-center gap-2 rounded-[var(--radius)] px-2 py-2.5 text-sm font-ui text-[var(--color-foreground)] transition-colors",
              "hover:bg-[var(--color-accent)] hover:text-[var(--color-accent-foreground)]",
              "data-[selected=true]:bg-[var(--color-accent)] data-[selected=true]:text-[var(--color-accent-foreground)]",
              "data-[disabled=true]:opacity-50 data-[disabled=true]:cursor-not-allowed",
              "[&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 [&_svg]:text-[var(--color-muted-foreground)]",
              "data-[selected=true]_[&_svg]:text-[var(--color-accent-foreground)]",
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
          className={cn(
              "ml-auto text-xs tracking-widest text-[var(--color-muted-foreground)] font-mono opacity-60",
              className
          )}
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
