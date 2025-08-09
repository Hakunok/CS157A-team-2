import * as React from "react"
import { cn } from "@/lib/utils.js"

function Table({ className, ...props }) {
  return (
      <div data-slot="table-container" className="relative w-full overflow-x-auto">
        <table
            data-slot="table"
            className={cn("w-full caption-bottom text-sm border-collapse", className)}
            {...props}
        />
      </div>
  )
}

function TableHeader({ className, ...props }) {
  return (
      <thead
          data-slot="table-header"
          className={cn("[&_tr]:border-b [&_tr]:border-[var(--color-border)]", className)}
          {...props}
      />
  )
}

function TableBody({ className, ...props }) {
  return (
      <tbody
          data-slot="table-body"
          className={cn("[&_tr:last-child]:border-0", className)}
          {...props}
      />
  )
}

function TableFooter({ className, ...props }) {
  return (
      <tfoot
          data-slot="table-footer"
          className={cn(
              "bg-[var(--color-muted)] border-t border-[var(--color-border)] font-medium [&>tr]:last:border-b-0",
              className
          )}
          {...props}
      />
  )
}

function TableRow({ className, ...props }) {
  return (
      <tr
          data-slot="table-row"
          className={cn(
              "transition-colors border-b border-[var(--color-border)] hover:bg-[var(--color-muted)]/40 data-[state=selected]:bg-[var(--color-muted)]",
              className
          )}
          {...props}
      />
  )
}

function TableHead({ className, ...props }) {
  return (
      <th
          data-slot="table-head"
          className={cn(
              "h-10 px-2 text-left align-middle text-xs uppercase tracking-wide font-medium text-[var(--color-muted-foreground)] font-ui whitespace-nowrap",
              "[&:has([role=checkbox])]:pr-0 [&>[role=checkbox]]:translate-y-[2px]",
              className
          )}
          {...props}
      />
  )
}

function TableCell({ className, ...props }) {
  return (
      <td
          data-slot="table-cell"
          className={cn(
              "px-2 py-2 align-middle text-sm text-[var(--color-foreground)] font-content whitespace-nowrap",
              "[&:has([role=checkbox])]:pr-0 [&>[role=checkbox]]:translate-y-[2px]",
              className
          )}
          {...props}
      />
  )
}

function TableCaption({ className, ...props }) {
  return (
      <caption
          data-slot="table-caption"
          className={cn("mt-4 text-sm text-[var(--color-muted-foreground)] font-content", className)}
          {...props}
      />
  )
}

export {
  Table,
  TableHeader,
  TableBody,
  TableFooter,
  TableHead,
  TableRow,
  TableCell,
  TableCaption,
}