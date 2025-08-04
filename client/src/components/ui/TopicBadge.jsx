import * as React from "react"
import { Badge } from "@/components/ui/badge"
import { X } from "lucide-react"
import * as Tooltip from "@radix-ui/react-tooltip"

export function TopicBadge({
  topic,
  onRemove,
  removable = false,
  variant = "default",
  onClick
}) {
  const isTruncated = topic.fullName.length > 20
  const content = isTruncated ? topic.code : topic.fullName
  const showRemoveButton = removable || variant === "removable"

  // Define variant styles
  const getVariantStyles = () => {
    switch (variant) {
      case "removable":
        return "bg-[var(--secondary-500)]/10 text-[var(--secondary-400)] border border-[var(--secondary-500)]/20 gap-1"
      case "default":
      default:
        return "bg-[var(--secondary-500)]/10 text-[var(--secondary-400)] border border-[var(--secondary-500)]/20 gap-1"
    }
  }

  const BadgeContent = ({ children, className }) => (
      <Badge
          className={className}
          onClick={onClick}
          style={{ cursor: onClick ? 'pointer' : 'default' }}
      >
        {children}
        {showRemoveButton && (
            <button
                onClick={(e) => {
                  e.stopPropagation()
                  onRemove?.(topic.topicId)
                }}
                className={
                  variant === "removable"
                      ? "ml-1 hover:bg-[var(--secondary-500)]/20 rounded-full p-0.5"
                      : "ml-1 hover:bg-[var(--secondary-500)]/20 rounded-full p-0.5"
                }
            >
              <X className="w-3 h-3" />
            </button>
        )}
      </Badge>
  )

  if (!isTruncated && !showRemoveButton) {
    return (
        <BadgeContent className={getVariantStyles()}>
          {content}
        </BadgeContent>
    )
  }

  return (
      <Tooltip.Provider>
        <Tooltip.Root>
          <Tooltip.Trigger asChild>
            <BadgeContent className={`${getVariantStyles()} ${isTruncated ? 'cursor-help' : ''}`}>
              {content}
            </BadgeContent>
          </Tooltip.Trigger>
          <Tooltip.Portal>
            <Tooltip.Content
                side="top"
                align="center"
                className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[var(--color-popover)] text-[var(--color-popover-foreground)] border border-[var(--color-border)] animate-in fade-in zoom-in"
            >
              {topic.fullName}
              <Tooltip.Arrow className="fill-[var(--color-border)]" />
            </Tooltip.Content>
          </Tooltip.Portal>
        </Tooltip.Root>
      </Tooltip.Provider>
  )
}