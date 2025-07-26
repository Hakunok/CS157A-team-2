import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva } from "class-variance-authority"
import { cn } from "@/lib/utils"

const badgeVariants = cva(
    "inline-flex items-center justify-center rounded-[var(--radius)] border px-2 py-0.5 text-xs font-medium w-fit whitespace-nowrap shrink-0 gap-1 overflow-hidden",
    {
      variants: {
        variant: {
          default:
              "bg-[--primary] text-[--primary-foreground] border-transparent",
          secondary:
              "bg-[--secondary] text-[--secondary-foreground] border-transparent",
          outline:
              "bg-transparent text-[--foreground] border border-[--border]",
          muted:
              "bg-[--muted] text-[--muted-foreground] border-transparent",
          destructive:
              "bg-[--destructive] text-white border-transparent",
        },
      },
      defaultVariants: {
        variant: "default",
      },
    }
)

function Badge({
  className,
  variant,
  asChild = false,
  ...props
}) {
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
