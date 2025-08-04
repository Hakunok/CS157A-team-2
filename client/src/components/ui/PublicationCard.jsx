import React from "react"
import { Link } from "react-router-dom"
import {
  Bookmark,
  Eye,
  Heart,
  Pen,
  Upload
} from "lucide-react"
import * as Tooltip from "@radix-ui/react-tooltip"

import { Button } from "@/components/ui/button"
import { useAuth } from "@/context/AuthContext"
import { cn } from "@/lib/utils"
import { format } from "date-fns"
import { KindBadge } from "@/components/ui/KindBadge"
import { TopicBadge } from "@/components/ui/TopicBadge"

function toDate(input) {
  if (!input) return null
  if (Array.isArray(input)) {
    const [y, m, d, h = 0, min = 0] = input
    return new Date(y, m - 1, d, h, min)
  }
  return new Date(input)
}

function formatCount(n) {
  if (n < 1000) return n
  const units = ["k", "M", "B"]
  let unitIndex = -1
  let count = n
  while (count >= 1000 && unitIndex < units.length - 1) {
    count /= 1000
    unitIndex++
  }
  return `${count.toFixed(1)}${units[unitIndex]}`
}

function PublicationCard({ publication, variant, onEdit, onPublish }) {
  const { isAuthenticated } = useAuth()
  const [saved, setSaved] = React.useState(false)
  const isDraft = variant === "draft"

  const {
    pubId,
    title,
    kind,
    publishedAt,
    viewCount,
    likeCount,
    authors,
    topics
  } = publication

  const author = authors?.[0]

  return (
      <Tooltip.Provider>
        <div
            className={cn(
                "rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-5",
                isDraft
                    ? "w-full min-h-[200px] flex flex-col"
                    : "w-full space-y-4"
            )}
        >
          <div className={cn(isDraft ? "flex flex-col flex-1 space-y-3" : "space-y-4")}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 flex-wrap">
                <KindBadge kind={kind} />
                {/* Topics badges next to kind badge */}
                {variant === "published" && topics?.length > 0 && (
                    topics.map((t) => (
                        <TopicBadge key={t.topicId} topic={t} />
                    ))
                )}
              </div>
              {variant === "published" && (
                  <Tooltip.Root>
                    <Tooltip.Trigger asChild>
                      <button
                          onClick={() => setSaved((s) => !s)}
                          className={cn(
                              "transition-all duration-200 p-1 rounded-sm",
                              saved
                                  ? "text-[var(--color-primary)] bg-[var(--color-primary)]/10"
                                  : "text-muted-foreground hover:text-[var(--color-primary)] hover:bg-[var(--color-primary)]/5"
                          )}
                          disabled={!isAuthenticated}
                      >
                        <Bookmark
                            className={cn(
                                "w-5 h-5 transition-all duration-200",
                                saved && "fill-[var(--color-primary)]"
                            )}
                        />
                      </button>
                    </Tooltip.Trigger>
                    <Tooltip.Portal>
                      <Tooltip.Content
                          side="top"
                          align="center"
                          className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[var(--color-popover)] text-[var(--color-popover-foreground)] border border-[var(--color-border)] animate-in fade-in zoom-in"
                      >
                        {saved ? "Unsave" : "Save"}
                        <Tooltip.Arrow className="fill-[var(--color-border)]" />
                      </Tooltip.Content>
                    </Tooltip.Portal>
                  </Tooltip.Root>
              )}
            </div>

            <h3 className="text-xl font-semibold font-content leading-tight break-words">
              {variant === "published" ? (
                  <Link
                      to={`/publications/${pubId}`}
                      className="hover:underline decoration-2 underline-offset-2 transition-colors"
                  >
                    {title.slice(0, 150)}
                  </Link>
              ) : (
                  title.slice(0, 150)
              )}
            </h3>

            {!isDraft && (
                <div className="text-sm text-muted-foreground font-ui flex items-center gap-2 flex-wrap">
                  {author && <span className="font-medium">{author.fullName}</span>}
                  {toDate(publishedAt) && (
                      <span>• {format(toDate(publishedAt), "MMMM d, yyyy")}</span>
                  )}
                </div>
            )}

            {!isDraft && (
                <div className="text-sm text-muted-foreground font-ui flex items-center gap-4 pt-2">
                <span className="flex items-center gap-2">
                  <Eye className="h-4 w-4" />
                  <span className="font-medium">{formatCount(viewCount)}</span>
                </span>
                  <span className="flex items-center gap-2">
                  <Heart className="h-4 w-4" />
                  <span className="font-medium">{formatCount(likeCount)}</span>
                </span>
                </div>
            )}

            {isDraft && <div className="flex-1" />}
          </div>

          {isDraft && (
              <div className="flex gap-2 pt-2 mt-auto">
                <Button
                    variant="outline2"
                    size="sm"
                    onClick={onEdit}
                    className="p-2 border-[var(--color-secondary)] text-[var(--color-secondary)] hover:bg-[var(--color-secondary)] hover:text-[var(--color-secondary-foreground)]"
                >
                  <Pen className="h-4 w-4" />
                  Edit
                </Button>

                <Button
                    variant="outline"
                    size="sm"
                    onClick={onPublish}
                    className="p-2 border-[var(--color-primary)] text-[var(--color-primary)] hover:bg-[var(--color-primary)] hover:text-[var(--color-primary-foreground)]"
                >
                  <Upload className="h-4 w-4" />
                  Publish
                </Button>
              </div>
          )}
        </div>
      </Tooltip.Provider>
  )
}

export { PublicationCard }