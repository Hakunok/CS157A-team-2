import React from "react";
import { Globe, Lock } from "lucide-react";
import * as Tooltip from "@radix-ui/react-tooltip";
import { Badge } from "@/components/badge.jsx";

export function VisibilityBadge({ isPublic }) {
  return (
      <Tooltip.Provider>
        <Tooltip.Root>
          <Tooltip.Trigger asChild>
            <Badge
                variant="outline"
                className="text-xs font-medium cursor-default px-2 py-1"
            >
              {isPublic ? (
                  <Globe className="w-3.5 h-3.5 text-[var(--color-secondary)]" />
              ) : (
                  <Lock className="w-3.5 h-3.5 text-[var(--muted-foreground)]" />
              )}
            </Badge>
          </Tooltip.Trigger>
          <Tooltip.Portal>
            <Tooltip.Content
                side="top"
                align="center"
                className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[var(--color-popover)]
            text-[var(--color-popover-foreground)] border border-[var(--color-border)] animate-in fade-in zoom-in"
            >
              {isPublic ? "Visible to everyone" : "Only visible to you"}
              <Tooltip.Arrow className="fill-[var(--color-border)]" />
            </Tooltip.Content>
          </Tooltip.Portal>
        </Tooltip.Root>
      </Tooltip.Provider>
  );
}