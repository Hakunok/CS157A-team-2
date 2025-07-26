import * as React from "react"
import { cn } from "@/lib/utils"

function Input({ className, type = "text", ...props }) {
  return (
      <input
          type={type}
          data-slot="input"
          className={cn(
              "font-ui font-normal text-sm w-full h-9 px-3 py-2 rounded-[var(--radius)]",
              "border border-[--border] bg-input text-foreground placeholder:text-muted-foreground",
              "transition-colors duration-200 ease-in-out shadow-sm",
              "focus:outline-none focus-visible:border-transparent focus-visible:ring-2 focus-visible:ring-[--ring]",
              "disabled:opacity-50 disabled:cursor-not-allowed",
              "aria-invalid:border-destructive aria-invalid:ring-destructive/30 dark:aria-invalid:ring-destructive/40",
              className
          )}
          {...props}
      />

  )
}

export { Input }
