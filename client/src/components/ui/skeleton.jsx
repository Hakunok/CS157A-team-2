import { cn } from "@/lib/utils"

function Skeleton({ className, ...props }) {
  return (
      <div
          data-slot="skeleton"
          className={cn(
              "bg-[var(--color-accent)] animate-pulse rounded-[var(--radius)]",
              className
          )}
          {...props}
      />
  )
}

export { Skeleton }
