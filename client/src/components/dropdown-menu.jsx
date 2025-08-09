"use client"

import * as React from "react"
import * as DropdownMenuPrimitive from "@radix-ui/react-dropdown-menu"
import { CheckIcon, ChevronRightIcon, CircleIcon } from "lucide-react"
import { cn } from "@/lib/utils.js"

function DropdownMenu(props) {
  return <DropdownMenuPrimitive.Root data-slot="dropdown-menu" {...props} />
}

function DropdownMenuPortal(props) {
  return <DropdownMenuPrimitive.Portal data-slot="dropdown-menu-portal" {...props} />
}

function DropdownMenuTrigger(props) {
  return <DropdownMenuPrimitive.Trigger data-slot="dropdown-menu-trigger" {...props} />
}

function DropdownMenuContent({ className, sideOffset = 4, ...props }) {
  return (
      <DropdownMenuPrimitive.Portal>
        <DropdownMenuPrimitive.Content
            data-slot="dropdown-menu-content"
            sideOffset={sideOffset}
            className={cn(
                "z-50 min-w-[8rem] overflow-hidden rounded-[var(--radius)] p-1",
                "bg-[var(--color-popover)]/95 backdrop-blur-sm text-[var(--color-popover-foreground)]",
                "shadow-lg",
                "focus-visible:outline-none",
                "data-[state=open]:animate-in data-[state=closed]:animate-out",
                "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
                "data-[state=open]:zoom-in-95 data-[state=closed]:zoom-out-95",
                className
            )}
            {...props}
        />
      </DropdownMenuPrimitive.Portal>
  )
}

function DropdownMenuGroup(props) {
  return <DropdownMenuPrimitive.Group data-slot="dropdown-menu-group" {...props} />
}

function DropdownMenuItem({ className, inset, variant = "default", ...props }) {
  return (
      <DropdownMenuPrimitive.Item
          data-slot="dropdown-menu-item"
          data-inset={inset}
          data-variant={variant}
          className={cn(
              "relative flex cursor-pointer items-center gap-2 select-none font-ui text-sm rounded-[var(--radius)] px-2 py-2 transition-all duration-200",
              "hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]",
              "focus:bg-[var(--color-muted)]/60 focus:text-[var(--color-foreground)]",
              "focus-visible:outline-none",
              "data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
              "data-[variant=destructive]:text-[var(--color-destructive)] data-[variant=destructive]:hover:bg-[var(--color-destructive)]/10 "
              + "data-[variant=destructive]:hover:text-[var(--color-destructive)]",
              "data-[inset]:pl-8",
              className
          )}
          {...props}
      />
  )
}

function DropdownMenuCheckboxItem({ className, children, checked, ...props }) {
  return (
      <DropdownMenuPrimitive.CheckboxItem
          data-slot="dropdown-menu-checkbox-item"
          className={cn(
              "relative flex cursor-pointer items-center gap-2 select-none font-ui text-sm rounded-[var(--radius)] py-2 pr-2 pl-8 "
              + "transition-all duration-200",
              "hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]",
              "focus:bg-[var(--color-muted)]/60 focus:text-[var(--color-foreground)]",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)]",
              "data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
              className
          )}
          checked={checked}
          {...props}
      >
      <span className="pointer-events-none absolute left-2 flex size-3.5 items-center justify-center">
        <DropdownMenuPrimitive.ItemIndicator>
          <CheckIcon className="size-4" />
        </DropdownMenuPrimitive.ItemIndicator>
      </span>
        {children}
      </DropdownMenuPrimitive.CheckboxItem>
  )
}

function DropdownMenuRadioGroup(props) {
  return <DropdownMenuPrimitive.RadioGroup data-slot="dropdown-menu-radio-group" {...props} />
}

function DropdownMenuRadioItem({ className, children, ...props }) {
  return (
      <DropdownMenuPrimitive.RadioItem
          data-slot="dropdown-menu-radio-item"
          className={cn(
              "relative flex cursor-pointer items-center gap-2 select-none font-ui text-sm rounded-[var(--radius)] py-2 pr-2 pl-8 "
              + "transition-all duration-200",
              "hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]",
              "focus:bg-[var(--color-muted)]/60 focus:text-[var(--color-foreground)]",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)]",
              "data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
              className
          )}
          {...props}
      >
      <span className="pointer-events-none absolute left-2 flex size-3.5 items-center justify-center">
        <DropdownMenuPrimitive.ItemIndicator>
          <CircleIcon className="size-2 fill-current" />
        </DropdownMenuPrimitive.ItemIndicator>
      </span>
        {children}
      </DropdownMenuPrimitive.RadioItem>
  )
}

function DropdownMenuLabel({ className, inset, ...props }) {
  return (
      <DropdownMenuPrimitive.Label
          data-slot="dropdown-menu-label"
          data-inset={inset}
          className={cn(
              "px-2 py-1.5 text-sm font-medium font-ui text-[var(--color-muted-foreground)]",
              "data-[inset]:pl-8",
              className
          )}
          {...props}
      />
  )
}

function DropdownMenuSeparator({ className, ...props }) {
  return (
      <DropdownMenuPrimitive.Separator
          data-slot="dropdown-menu-separator"
          className={cn("bg-[var(--color-border)]/40 -mx-1 my-1 h-px", className)}
          {...props}
      />
  )
}

function DropdownMenuShortcut({ className, ...props }) {
  return (
      <span
          data-slot="dropdown-menu-shortcut"
          className={cn("ml-auto text-xs tracking-widest text-[var(--color-muted-foreground)]", className)}
          {...props}
      />
  )
}

function DropdownMenuSub(props) {
  return <DropdownMenuPrimitive.Sub data-slot="dropdown-menu-sub" {...props} />
}

function DropdownMenuSubTrigger({ className, inset, children, ...props }) {
  return (
      <DropdownMenuPrimitive.SubTrigger
          data-slot="dropdown-menu-sub-trigger"
          data-inset={inset}
          className={cn(
              "flex cursor-pointer items-center select-none font-ui text-sm rounded-[var(--radius)] px-2 py-2 transition-all duration-200",
              "hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]",
              "focus:bg-[var(--color-muted)]/60 focus:text-[var(--color-foreground)]",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)]",
              "data-[state=open]:bg-[var(--color-muted)]/80 data-[state=open]:text-[var(--color-foreground)]",
              "data-[inset]:pl-8",
              className
          )}
          {...props}
      >
        {children}
        <ChevronRightIcon className="ml-auto size-4" />
      </DropdownMenuPrimitive.SubTrigger>
  )
}

function DropdownMenuSubContent({ className, ...props }) {
  return (
      <DropdownMenuPrimitive.SubContent
          data-slot="dropdown-menu-sub-content"
          className={cn(
              "z-50 min-w-[8rem] overflow-hidden rounded-[var(--radius)] border border-[var(--color-border)]/30 bg-[var(--color-popover)]/95 "
              + "backdrop-blur-sm text-[var(--color-popover-foreground)] p-1 shadow-lg",
              "data-[state=open]:animate-in data-[state=closed]:animate-out",
              "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
              "data-[state=open]:zoom-in-95 data-[state=closed]:zoom-out-95",
              className
          )}
          {...props}
      />
  )
}

export {
  DropdownMenu,
  DropdownMenuPortal,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuLabel,
  DropdownMenuItem,
  DropdownMenuCheckboxItem,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
}