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
              "size-4 shrink-0 rounded-[var(--radius)] border border-[--border] shadow-sm",

              // Unchecked background
              "bg-input text-foreground",

              // Checked state
              "data-[state=checked]:bg-[--primary] data-[state=checked]:text-[--primary-foreground] data-[state=checked]:border-[--primary]",

              // Focus ring
              "focus:outline-none focus-visible:ring-2 focus-visible:ring-[--ring]",

              // Validation state
              "aria-invalid:border-[--destructive] aria-invalid:ring-[--destructive]/30",

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
