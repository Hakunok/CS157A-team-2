import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva } from "class-variance-authority"
import { cn } from "@/lib/utils.js"

const badgeVariants = cva(
    "inline-flex items-center justify-center rounded-[var(--radius)] border px-2 py-0.5 text-xs "
    + "font-medium w-fit whitespace-nowrap shrink-0 gap-1 overflow-hidden ring-offset-background focus-visible:outline-none focus-visible:ring-2 "
    + "focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2",
    {
      variants: {
        variant: {
          default:
              "bg-[var(--color-primary)] text-[var(--color-primary-foreground)] border-transparent",
          secondary:
              "bg-[var(--color-secondary)] text-[var(--color-secondary-foreground)] border-transparent",
          outline:
              "bg-transparent text-[var(--color-foreground)] border border-[var(--color-border)]",
          muted:
              "bg-[var(--color-muted)] text-[var(--color-muted-foreground)] border-transparent",
          destructive:
              "bg-[var(--color-destructive)] text-white border-transparent",
        },
      },
      defaultVariants: {
        variant: "default",
      },
    }
)

function Badge({ className, variant, asChild = false, ...props }) {
  const Comp = asChild ? Slot : "span"

  return (
      <Comp
          data-slot="badge"
          className={cn(badgeVariants({ variant }), className)}
          {...props}
      />
  )
}

export { Badge, badgeVariants }
