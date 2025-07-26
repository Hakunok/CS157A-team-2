// File: components/ui/card.jsx
import * as React from "react"
import { cn } from "@/lib/utils"

function Card({ className, ...props }) {
  return (
      <div
          data-slot="card"
          className={cn(
              "bg-card text-card-foreground flex flex-col gap-6 rounded-md px-6 py-6 shadow-sm",
              "border border-[--border]",
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
              "font-ui grid auto-rows-min grid-rows-[auto_auto] items-start gap-1.5 @container/card-header has-data-[slot=card-action]:grid-cols-[1fr_auto] pb-4",
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
              "font-ui text-xl font-semibold leading-tight tracking-tight",
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
          className={cn("font-content text-muted-foreground text-sm leading-normal", className)}
          {...props}
      />
  )
}

function CardAction({ className, ...props }) {
  return (
      <div
          data-slot="card-action"
          className={cn("col-start-2 row-span-2 row-start-1 self-start justify-self-end", className)}
          {...props}
      />
  )
}

function CardContent({ className, ...props }) {
  return (
      <div
          data-slot="card-content"
          className={cn("font-content text-base leading-relaxed", className)}
          {...props}
      />
  )
}

function CardFooter({ className, ...props }) {
  return (
      <div
          data-slot="card-footer"
          className={cn("flex items-center pt-4 border-t border-[--border]", className)}
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
