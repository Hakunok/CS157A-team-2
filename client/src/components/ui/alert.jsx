import * as React from "react"
import { cva } from "class-variance-authority"
import { cn } from "@/lib/utils"

const alertVariants = cva(
    "relative w-full rounded-[var(--radius)] border px-4 py-3 text-sm grid",
    {
      variants: {
        variant: {
          default: [
            "bg-[var(--color-card)] text-[var(--color-card-foreground)]",
            "has-[>svg]:grid-cols-[1.5rem_1fr]",
            "grid-cols-[0_1fr] has-[>svg]:gap-x-3 gap-y-0.5 items-start",
            "[&>svg]:size-4 [&>svg]:translate-y-0.5 [&>svg]:text-current",
          ].join(" "),
          destructive: [
            "bg-[var(--color-card)] text-[var(--color-destructive)]",
            "has-[>svg]:grid-cols-[1.5rem_1fr]",
            "grid-cols-[0_1fr] has-[>svg]:gap-x-3 gap-y-0.5 items-start",
            "[&>svg]:size-4 [&>svg]:translate-y-0.5 [&>svg]:text-[var(--color-destructive)]",
            "*:data-[slot=alert-description]:text-[var(--color-destructive)]/90",
          ].join(" "),
        },
      },
      defaultVariants: {
        variant: "default",
      },
    }
)

function Alert({ className, variant, ...props }) {
  return (
      <div
          data-slot="alert"
          role="alert"
          className={cn(alertVariants({ variant }), className)}
          {...props}
      />
  )
}

function AlertTitle({ className, ...props }) {
  return (
      <div
          data-slot="alert-title"
          className={cn(
              "col-start-2 line-clamp-1 min-h-4 font-medium tracking-tight",
              className
          )}
          {...props}
      />
  )
}

function AlertDescription({ className, ...props }) {
  return (
      <div
          data-slot="alert-description"
          className={cn(
              "col-start-2 grid justify-items-start gap-1 text-sm text-[var(--color-muted-foreground)] [&_p]:leading-relaxed",
              className
          )}
          {...props}
      />
  )
}

export { Alert, AlertTitle, AlertDescription }
