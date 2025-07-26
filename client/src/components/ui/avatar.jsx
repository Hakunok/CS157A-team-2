import * as React from "react"
import * as AvatarPrimitive from "@radix-ui/react-avatar"
import { cn } from "@/lib/utils"

function Avatar({ className, ...props }) {
  return (
      <AvatarPrimitive.Root
          data-slot="avatar"
          className={cn(
              "relative flex size-8 shrink-0 overflow-hidden rounded-[var(--radius)] bg-muted text-muted-foreground font-ui text-sm font-medium",
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
              "flex size-full items-center justify-center rounded-[var(--radius)] bg-muted text-muted-foreground font-ui text-sm font-medium",
              className
          )}
          {...props}
      >
        {initials}
      </AvatarPrimitive.Fallback>
  )
}

export { Avatar, AvatarImage, AvatarFallback }
