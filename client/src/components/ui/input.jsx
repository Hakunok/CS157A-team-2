import * as React from "react"
import { cn } from "@/lib/utils"

function Input({ className, type = "text", ...props }) {
  return (
      <input
          type={type}
          data-slot="input"
          className={cn(
              "w-full h-9 px-3 py-2 text-sm font-ui font-normal",
              "rounded-[var(--radius)] border border-[var(--color-border)]",
              "bg-[var(--color-input)] text-[var(--color-foreground)] placeholder:text-[var(--color-muted-foreground)]",
              "transition-colors duration-200 ease-in-out shadow-sm",
              "focus:outline-none focus-visible:ring-2",
              "focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-1 focus-visible:ring-offset-[var(--color-surface)]",
              "aria-invalid:border-[var(--color-destructive)]",
              "aria-invalid:ring-[var(--color-destructive)]/30 dark:aria-invalid:ring-[var(--color-destructive)]/40",
              "disabled:opacity-50 disabled:cursor-not-allowed",
              className
          )}
          {...props}
      />
  )
}

export { Input }
