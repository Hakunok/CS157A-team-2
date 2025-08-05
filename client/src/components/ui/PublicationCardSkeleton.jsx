import { cn } from "@/lib/utils";

export function PublicationCardSkeleton() {
  return (
      <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-5 space-y-4">
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="h-5 w-20 rounded-full bg-[var(--color-muted)]/60 animate-pulse" />
              <div className="h-5 w-24 rounded-full bg-[var(--color-muted)]/60 animate-pulse" />
            </div>
            <div className="h-6 w-6 rounded-sm bg-[var(--color-muted)]/60 animate-pulse" />
          </div>

          <div className="space-y-2">
            <div className="h-5 w-full rounded-md bg-[var(--color-muted)]/60 animate-pulse" />
            <div className="h-5 w-3/4 rounded-md bg-[var(--color-muted)]/60 animate-pulse" />
          </div>

          <div className="h-4 w-1/2 rounded-md bg-[var(--color-muted)]/60 animate-pulse" />

          <div className="flex items-center gap-4 pt-2">
            <div className="h-4 w-12 rounded-md bg-[var(--color-muted)]/60 animate-pulse" />
            <div className="h-4 w-12 rounded-md bg-[var(--color-muted)]/60 animate-pulse" />
          </div>
        </div>
      </div>
  );
}