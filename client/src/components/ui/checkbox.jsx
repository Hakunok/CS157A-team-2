import * as React from "react"
import * as CheckboxPrimitive from "@radix-ui/react-checkbox"
import { CheckIcon } from "lucide-react"
import { cn } from "@/lib/utils"

function Checkbox({ className, ...props }) {
  return (
      <CheckboxPrimitive.Root
          data-slot="checkbox"
          className={cn(
              // Size and structure
              "size-4 shrink-0 rounded-[var(--radius)] border border-[var(--color-border)] shadow-sm",

              // Unchecked background
              "bg-[var(--color-input)] text-[var(--color-foreground)]",

              // Checked state
              "data-[state=checked]:bg-[var(--color-primary)] data-[state=checked]:text-[var(--color-primary-foreground)] data-[state=checked]:border-[var(--color-primary)]",

              // Focus ring
              "focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2 ring-offset-background",

              // Validation state
              "aria-invalid:border-[var(--color-destructive)] aria-invalid:ring-[var(--color-destructive)]/30",

              // Disabled state
              "disabled:cursor-not-allowed disabled:opacity-50",

              // Motion
              "transition-colors duration-150 ease-in-out",

              className
          )}
          {...props}
      >
        <CheckboxPrimitive.Indicator
            data-slot="checkbox-indicator"
            className="flex items-center justify-center text-current"
        >
          <CheckIcon className="size-3.5" />
        </CheckboxPrimitive.Indicator>
      </CheckboxPrimitive.Root>
  )
}

export { Checkbox }
