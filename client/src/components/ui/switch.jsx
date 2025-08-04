import * as React from "react";
import * as SwitchPrimitive from "@radix-ui/react-switch";
import { cn } from "@/lib/utils";

function Switch({ className, ...props }) {
  return (
      <SwitchPrimitive.Root
          data-slot="switch"
          className={cn(
              // Background when checked or unchecked
              "peer inline-flex h-[1.15rem] w-8 shrink-0 items-center rounded-full border border-transparent shadow-xs transition-all outline-none",
              "data-[state=checked]:bg-[var(--color-primary)]",
              "data-[state=unchecked]:bg-[var(--color-input)]",
              "focus-visible:ring-[3px] focus-visible:ring-[var(--color-ring)] focus-visible:border-[var(--color-ring)]",
              "disabled:cursor-not-allowed disabled:opacity-50",
              className
          )}
          {...props}
      >
        <SwitchPrimitive.Thumb
            data-slot="switch-thumb"
            className={cn(
                "pointer-events-none block size-4 rounded-full transition-transform ring-0",
                "bg-[var(--color-background)]",
                "data-[state=checked]:translate-x-[calc(100%-2px)]",
                "data-[state=unchecked]:translate-x-0"
            )}
        />
      </SwitchPrimitive.Root>
  );
}

export { Switch };