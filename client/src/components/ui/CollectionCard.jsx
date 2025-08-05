import React from "react";
import { Link } from "react-router-dom";
import { Pencil, Trash } from "lucide-react";
import { format } from "date-fns";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { KindBadge } from "@/components/ui/KindBadge";

function toDate(input) {
  if (!input) return null;
  if (typeof input.toDate === "function") {
    return input.toDate();
  }
  if (Array.isArray(input)) {
    const [y, m, d, h = 0, min = 0] = input;
    return new Date(y, m - 1, d, h, min);
  }
  const date = new Date(input);
  if (isNaN(date.getTime())) {
    return null;
  }
  return date;
}

export function CollectionCard({
  collection,
  variant = "default",
  onEdit,
  onDelete,
  className,
}) {
  const creationDate = toDate(collection.createdAt);
  const isCompact = variant === "compact";

  const showActions = onEdit && onDelete && !collection.isDefault;

  return (
      <Card
          className={cn(
              "border border-[var(--color-border)] rounded-lg bg-[var(--color-surface)] w-full flex flex-col",
              isCompact ? "p-4" : "p-5",
              className
          )}
      >
        <div className="flex flex-col space-y-3 flex-grow">
          <div className="flex justify-between items-start gap-2">
            <div className="flex items-center gap-2 flex-wrap">
              <KindBadge kind="COLLECTION" />
              {collection.isDefault && (
                  <Badge className="bg-blue-500/10 text-blue-600 border border-blue-500/20 hover:bg-blue-500/20 text-xs font-medium cursor-default">
                    Default
                  </Badge>
              )}
            </div>

            {showActions && (
                <div className="flex items-center gap-2">
                  <Button
                      variant="ghost"
                      size="icon"
                      className="size-6"
                      onClick={(e) => {
                        e.preventDefault();
                        onEdit();
                      }}
                  >
                    <Pencil className="size-4" />
                  </Button>
                  <Button
                      variant="ghost"
                      size="icon"
                      className="size-6 text-destructive"
                      onClick={(e) => {
                        e.preventDefault();
                        onDelete();
                      }}
                  >
                    <Trash className="size-4" />
                  </Button>
                </div>
            )}
          </div>

          <h3
              className={cn(
                  "font-semibold font-content leading-tight break-words text-xl",
                  isCompact && "line-clamp-2"
              )}
          >
            <Link
                to={`/collections/${collection.collectionId}`}
                className="hover:underline decoration-2 underline-offset-2 transition-colors"
            >
              {collection.title}
            </Link>
          </h3>

          <div
              className={cn(
                  "text-muted-foreground font-ui text-sm"
              )}
          >
            {creationDate && (
                <span>Created {format(creationDate, "MMM d, yyyy")}</span>
            )}
          </div>
        </div>
      </Card>
  );
}
