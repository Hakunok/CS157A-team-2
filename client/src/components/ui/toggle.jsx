import * as React from "react";
import * as TogglePrimitive from "@radix-ui/react-toggle";
import { cva } from "class-variance-authority";
import { cn } from "@/lib/utils";

const toggleVariants = cva(
    "inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium transition-[color,box-shadow] outline-none whitespace-nowrap",
    {
      variants: {
        variant: {
          default: "",
          outline:
              "border border-[var(--color-border)] bg-transparent shadow-xs",
        },
        size: {
          default: "h-9 px-2 min-w-9",
          sm: "h-8 px-1.5 min-w-8",
          lg: "h-10 px-2.5 min-w-10",
        },
      },
      defaultVariants: {
        variant: "default",
        size: "default",
      },
    }
);

function Toggle({ className, variant, size, ...props }) {
  return (
      <TogglePrimitive.Root
          data-slot="toggle"
          className={cn(
              toggleVariants({ variant, size }),
              "hover:bg-[var(--color-muted)] hover:text-[var(--color-muted-foreground)]",
              "data-[state=on]:bg-[var(--color-accent)] data-[state=on]:text-[var(--color-accent-foreground)]",
              "focus-visible:ring-[3px] focus-visible:ring-[var(--color-ring)] focus-visible:border-[var(--color-ring)]",
              "disabled:pointer-events-none disabled:opacity-50",
              "[&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 [&_svg]:shrink-0",
              "aria-invalid:ring-[var(--color-destructive)/0.2] aria-invalid:border-[var(--color-destructive)]",
              className
          )}
          {...props}
      />
  );
}

export { Toggle, toggleVariants };