import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva } from "class-variance-authority"

import { cn } from "@/lib/utils"

const buttonVariants = cva(
    // Core button structure
    "font-ui inline-flex items-center justify-center gap-2 rounded-md font-medium transition-all duration-200 ease-in-out disabled:pointer-events-none disabled:opacity-50 focus:outline-none focus-visible:outline-none",
    {
      variants: {
        variant: {
          default:
              "bg-primary text-primary-foreground hover:bg-primary/85 hover:shadow-md hover:scale-[1.02] active:scale-[0.98] shadow-sm focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-0",
          destructive:
              "bg-destructive text-white hover:bg-destructive/85 hover:shadow-md hover:scale-[1.02] active:scale-[0.98] focus-visible:ring-2 focus-visible:ring-destructive/40",
          outline:
              "border border-border/60 bg-transparent text-foreground hover:border-primary/60 hover:bg-primary/10 hover:text-primary hover:shadow-sm hover:scale-[1.02] active:scale-[0.98] focus-visible:ring-2 focus-visible:ring-ring",
          secondary:
              "bg-secondary text-secondary-foreground hover:bg-secondary/85 hover:shadow-md hover:scale-[1.02] active:scale-[0.98] focus-visible:ring-2 focus-visible:ring-secondary/40",
          ghost:
              "bg-transparent text-foreground hover:bg-muted/60 hover:text-foreground hover:scale-[1.02] active:scale-[0.98] focus-visible:ring-2 focus-visible:ring-muted",
          link:
              "text-primary underline underline-offset-4 hover:text-primary/80 hover:underline-offset-2 focus-visible:ring-0",
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
          className={cn(buttonVariants({ variant, size, className }))}
          {...props}
      />
  )
}

export { Button, buttonVariants }