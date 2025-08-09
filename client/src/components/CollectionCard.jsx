import React from "react";
import { Link } from "react-router-dom";
import { Pencil, Trash } from "lucide-react";
import { format } from "date-fns";
import * as Tooltip from "@radix-ui/react-tooltip";

import { cn } from "@/lib/utils.js";
import { Button } from "@/components/button.jsx";
import { Card } from "@/components/card.jsx";
import { KindBadge, DefaultBadge } from "@/components/KindBadge.jsx";
import { VisibilityBadge } from "@/components/VisibilityBadge.jsx";

function toDate(input) {
  if (!input) return null;
  if (typeof input.toDate === "function") return input.toDate();
  if (Array.isArray(input)) {
    const [y, m, d, h = 0, min = 0] = input;
    return new Date(y, m - 1, d, h, min);
  }
  const date = new Date(input);
  return isNaN(date.getTime()) ? null : date;
}

export function CollectionCard({
  collection,
  onEdit,
  onDelete,
  className,
}) {
  const creationDate = toDate(collection.createdAt);
  const showActions = onEdit && onDelete && !collection.isDefault;

  return (
      <Tooltip.Provider delayDuration={700}>
        <Card
            className={cn(
                "w-full rounded-lg border border-[var(--color-border)] "
                + "bg-[var(--color-surface)] p-3 min-h-[130px] flex flex-col justify-between",
                className
            )}
        >
          {/* header: badges, buttons */}
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-1.5 flex-wrap">
              <KindBadge kind="COLLECTION" />
              {collection.isDefault ? (
                  <DefaultBadge />
              ) : (
                  <VisibilityBadge isPublic={collection.isPublic} />
              )}
            </div>

            {showActions && (
                <div className="flex items-center gap-1">
                  {/* edit button */}
                  <Tooltip.Root>
                    <Tooltip.Trigger asChild>
                      <Button
                          variant="ghost"
                          size="icon"
                          className="group size-6 text-[var(--color-secondary)] hover:text-[var(--color-secondary)] p-0"
                          onClick={(e) => {
                            e.preventDefault();
                            onEdit();
                          }}
                      >
                        <Pencil className="size-4 stroke-current fill-none group-hover:fill-[var(--color-secondary)]" />
                      </Button>
                    </Tooltip.Trigger>
                    <Tooltip.Portal>
                      <Tooltip.Content
                          side="top"
                          align="center"
                          className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[--color-popover]
                          text-[--color-popover-foreground] border border-[--color-border] animate-in fade-in zoom-in"
                      >
                        Edit Collection
                        <Tooltip.Arrow className="fill-[--color-border]" />
                      </Tooltip.Content>
                    </Tooltip.Portal>
                  </Tooltip.Root>

                  {/* delete button */}
                  <Tooltip.Root>
                    <Tooltip.Trigger asChild>
                      <Button
                          variant="ghost"
                          size="icon"
                          className="group size-6 text-destructive hover:text-destructive p-0"
                          onClick={(e) => {
                            e.preventDefault();
                            onDelete();
                          }}
                      >
                        <Trash className="size-4 stroke-current fill-none group-hover:fill-destructive" />
                      </Button>
                    </Tooltip.Trigger>
                    <Tooltip.Portal>
                      <Tooltip.Content
                          side="top"
                          align="center"
                          className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[--color-popover]
                          text-[--color-popover-foreground] border border-[--color-border] animate-in fade-in zoom-in"
                      >
                        Delete Collection
                        <Tooltip.Arrow className="fill-[--color-border]" />
                      </Tooltip.Content>
                    </Tooltip.Portal>
                  </Tooltip.Root>
                </div>
            )}
          </div>

          {/* title with fixed 2 line height */}
          <h3 className="text-xl font-semibold font-content leading-tight break-words line-clamp-2 min-h-[3.75rem]">
            <Link
                to={`/collections/${collection.collectionId}`}
                className="hover:underline decoration-2 underline-offset-2 transition-colors"
            >
              {collection.title}
            </Link>
          </h3>

          {/* Date */}
          <div className="text-xs text-muted-foreground font-ui">
            {creationDate && (
                <span>Created {format(creationDate, "MMM d, yyyy")}</span>
            )}
          </div>
        </Card>
      </Tooltip.Provider>
  );
}
