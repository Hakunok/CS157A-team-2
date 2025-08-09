import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva } from "class-variance-authority"

import { cn } from "@/lib/utils.js"

const buttonVariants = cva(
    "font-ui inline-flex items-center justify-center gap-2 rounded-[var(--radius)] font-medium "
    + "transition-all duration-200 ease-in-out disabled:pointer-events-none disabled:opacity-50 ring-offset-background "
    + "focus:outline-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2",
    {
      variants: {
        variant: {
          default:
              "bg-[var(--color-primary)] text-[var(--color-primary-foreground)] hover:bg-[color-mix(in srgb, "
              + "var(--color-primary) 85%, black)] hover:shadow-md hover:scale-[1.02] active:scale-[0.98] shadow-sm",
          destructive:
              "bg-[var(--color-destructive)] text-white hover:bg-[color-mix(in srgb, var(--color-destructive) 85%, black)] "
              + "hover:shadow-md hover:scale-[1.02] active:scale-[0.98]",
          outline:
              "border border-[var(--color-border)] bg-transparent text-[var(--color-foreground)] hover:border-[var(--color-primary)] "
              + "hover:bg-[color-mix(in srgb, var(--color-primary) 10%, black)] hover:text-[var(--color-primary)] hover:shadow-sm hover:scale-[1.02] "
              + "active:scale-[0.98]",
          outline2:
              "border border-[var(--color-border)] bg-transparent text-[var(--color-foreground)] hover:border-[var(--color-secondary] "
              + "hover:bg-[color-mix(in srgb, var(--color-secondary) 10%, black)] hover:text-[var(--color-secondary)] hover:shadow-sm hover:scale-[1.02] "
              + "active:scale-[0.98]",
          secondary:
              "bg-[var(--color-secondary)] text-[var(--color-secondary-foreground)] hover:bg-[color-mix(in srgb, var(--color-secondary) 85%, black)] "
              + "hover:shadow-md hover:scale-[1.02] active:scale-[0.98]",
          ghost:
              "bg-transparent text-[var(--color-foreground)] hover:bg-[var(--color-muted)] hover:text-[var(--color-foreground)] hover:scale-[1.02] "
              + "active:scale-[0.98]",
          link:
              "text-[var(--color-primary)] underline underline-offset-4 hover:text-[color-mix(in srgb, var(--color-primary) 80%, black)] "
              + "hover:underline-offset-2 focus-visible:ring-0",
        },
        size: {
          sm: "h-8 px-3 text-sm",
          default: "h-9 px-4 text-sm",
          lg: "h-10 px-5 text-base",
          xl: "h-11 px-6 text-lg",
          icon: "size-9 p-0",
        },
      },
      defaultVariants: {
        variant: "default",
        size: "default",
      },
    }
)

function Button({ className, variant, size, asChild = false, ...props }) {
  const Comp = asChild ? Slot : "button"
  return (
      <Comp
          data-slot="button"
          className={cn(buttonVariants({ variant, size }), className)}
          {...props}
      />
  )
}

export { Button, buttonVariants }