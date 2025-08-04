import * as React from "react"
import { cn } from "@/lib/utils"

function Textarea({ className, ...props }) {
  return (
      <textarea
          data-slot="textarea"
          className={cn(
              // Base appearance
              "w-full min-h-16 px-3 py-2 text-base md:text-sm font-ui rounded-[var(--radius)] shadow-xs border bg-transparent",

              // Colors from your token system
              "border-[var(--color-input)] bg-[var(--color-input)]/30 text-[var(--color-foreground)] placeholder:text-[var(--color-muted-foreground)]",

              // Transitions and interactivity
              "transition-[color,box-shadow] outline-none focus-visible:ring-[3px]",
              "focus-visible:ring-[var(--color-ring)]/50 focus-visible:border-[var(--color-ring)]",

              // Validation and disabled states
              "aria-invalid:border-[var(--color-destructive)] aria-invalid:ring-[var(--color-destructive)]/20 dark:aria-invalid:ring-[var(--color-destructive)]/40",
              "disabled:opacity-50 disabled:cursor-not-allowed",

              // Sizing behavior
              "flex field-sizing-content",

              className
          )}
          {...props}
      />
  )
}

export { Textarea }