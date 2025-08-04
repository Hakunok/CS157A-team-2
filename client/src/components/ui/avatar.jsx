import * as React from "react"
import * as AvatarPrimitive from "@radix-ui/react-avatar"
import { cn } from "@/lib/utils"

function Avatar({ className, ...props }) {
  return (
      <AvatarPrimitive.Root
          data-slot="avatar"
          className={cn(
              "relative flex size-8 shrink-0 overflow-hidden rounded-[var(--radius)] bg-[var(--color-muted)] text-[var(--color-muted-foreground)] font-ui text-sm font-medium ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2",
              className
          )}
          {...props}
      />
  )
}

function AvatarImage({ className, ...props }) {
  return (
      <AvatarPrimitive.Image
          data-slot="avatar-image"
          className={cn("aspect-square size-full object-cover", className)}
          {...props}
      />
  )
}

function AvatarFallback({ firstName, lastName, className, ...props }) {
  const initials = `${firstName?.[0] || ""}${lastName?.[0] || ""}`.toUpperCase()

  return (
      <AvatarPrimitive.Fallback
          data-slot="avatar-fallback"
          delayMs={300}
          className={cn(
              "flex size-full items-center justify-center rounded-[var(--radius)] bg-[var(--color-muted)] text-[var(--color-muted-foreground)] font-ui font-medium leading-none",
              className ?? "text-sm"
          )}
          {...props}
      >
        {initials}
      </AvatarPrimitive.Fallback>
  )
}

export { Avatar, AvatarImage, AvatarFallback }