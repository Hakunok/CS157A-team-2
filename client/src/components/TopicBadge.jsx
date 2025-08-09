import * as React from "react"
import { Badge } from "@/components/badge.jsx"
import { X } from "lucide-react"
import * as Tooltip from "@radix-ui/react-tooltip"
import { cn } from "@/lib/utils.js"

export function TopicBadge({
  topic,
  onRemove,
  isSelected = false,
  variant = "default",
  onClick
}) {
  const isTruncated = topic.fullName.length > 15
  const content = isTruncated ? topic.code : topic.fullName
  const showRemoveButton = (variant === "removable") && !isSelected

  const baseStyles = "transition-colors duration-200"
  const selectedStyles = "bg-[var(--color-primary)]/10 text-[var(--color-primary)] border border-[var(--color-primary)]/20 "
      + "hover:bg-[var(--color-primary)]/20"
  const defaultStyles = "bg-[var(--secondary-500)]/10 text-[var(--secondary-400)] border border-[var(--secondary-500)]/20 "
      + "hover:bg-[var(--secondary-500)]/20"
  const removableStyles = `${defaultStyles} gap-1`

  const badgeClass = cn(
      getVariantStyles(),
      isTruncated ? "cursor-help" : "",
      "inline-flex items-center"
  )

  function getVariantStyles() {
    if (isSelected) return cn(baseStyles, selectedStyles)
    return variant === "removable" ? cn(baseStyles, removableStyles) : cn(baseStyles, defaultStyles)
  }

  const handleRemove = (e) => {
    e.stopPropagation()
    onRemove?.(topic.topicId)
  }

  const badgeContent = (
      <Badge
          onClick={onClick}
          className={badgeClass}
          style={{ cursor: onClick ? "pointer" : "default" }}
      >
        {content}
        {showRemoveButton && (
            <button
                onClick={handleRemove}
                className="ml-1 hover:bg-[var(--secondary-500)]/20 rounded-full p-0.5"
            >
              <X className="w-3 h-3" />
            </button>
        )}
      </Badge>
  )

  if (!isTruncated) return badgeContent

  return (
      <Tooltip.Provider>
        <Tooltip.Root>
          <Tooltip.Trigger asChild>
            {badgeContent}
          </Tooltip.Trigger>
          <Tooltip.Portal>
            <Tooltip.Content
                side="top"
                align="center"
                className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[var(--color-popover)] text-[var(--color-popover-foreground)]
                border border-[var(--color-border)] animate-in fade-in zoom-in"
            >
              {topic.fullName}
              <Tooltip.Arrow className="fill-[var(--color-border)]" />
            </Tooltip.Content>
          </Tooltip.Portal>
        </Tooltip.Root>
      </Tooltip.Provider>
  )
}
