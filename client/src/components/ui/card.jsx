import * as React from "react"
import { cn } from "@/lib/utils"

function Card({ className, ...props }) {
  return (
      <div
          data-slot="card"
          className={cn(
              "bg-[var(--color-card)] text-[var(--color-card-foreground)] flex flex-col gap-6",
              "rounded-[var(--radius)] px-6 py-6 shadow-sm border border-[var(--color-border)] ring-offset-background focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2",
              className
          )}
          {...props}
      />
  )
}

function CardHeader({ className, ...props }) {
  return (
      <div
          data-slot="card-header"
          className={cn(
              "font-ui grid auto-rows-min items-start gap-1.5 pb-4",
              "@container/card-header",
              "has-[data-slot=card-action]:grid-cols-[1fr_auto] grid-rows-[auto_auto]",
              className
          )}
          {...props}
      />
  )
}

function CardTitle({ className, ...props }) {
  return (
      <h3
          data-slot="card-title"
          className={cn(
              "font-ui text-xl font-semibold tracking-tight leading-snug text-[var(--color-foreground)]",
              className
          )}
          {...props}
      />
  )
}

function CardDescription({ className, ...props }) {
  return (
      <p
          data-slot="card-description"
          className={cn(
              "font-content text-sm leading-normal text-[var(--color-muted-foreground)]",
              className
          )}
          {...props}
      />
  )
}

function CardAction({ className, ...props }) {
  return (
      <div
          data-slot="card-action"
          className={cn(
              "col-start-2 row-span-2 row-start-1 self-start justify-self-end",
              className
          )}
          {...props}
      />
  )
}

function CardContent({ className, ...props }) {
  return (
      <div
          data-slot="card-content"
          className={cn(
              "font-content text-base leading-relaxed text-[var(--color-foreground)]",
              className
          )}
          {...props}
      />
  )
}

function CardFooter({ className, ...props }) {
  return (
      <div
          data-slot="card-footer"
          className={cn(
              "flex items-center pt-4 border-t border-[var(--color-border)]",
              className
          )}
          {...props}
      />
  )
}

export {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardAction,
  CardContent,
  CardFooter,
}