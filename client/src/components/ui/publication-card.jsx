import * as React from "react"
import { Link } from "react-router-dom"
import { Eye, Heart, Bookmark, BookmarkCheck } from "lucide-react"
import { cn } from "@/lib/utils"

import {
  Card,
  CardHeader,
  CardFooter,
  CardTitle,
  CardAction,
  CardContent
} from "@/components/ui/card"

export function PublicationCard({ publication, variant = "default", className, ...props }) {
  const {
    pubId,
    title,
    authors = [],
    publishDate,
    views = 0,
    likes = 0,
    tags = [],
    abstract,
    isInReadingList = false
  } = publication

  const formatAuthors = (authorList, maxShow = 3) => {
    if (authorList.length <= maxShow) return authorList.join(", ")
    return authorList.slice(0, maxShow).join(", ") + "..."
  }

  const truncateText = (text, maxLength = 200) => {
    if (!text || text.length <= maxLength) return text
    return text.slice(0, maxLength).trim() + "..."
  }

  if (variant === "compact") {
    return (
        <Card className={cn("hover:shadow-md transition-all duration-200", className)} {...props}>
          <CardHeader className="pb-3">
            <div className="flex items-start justify-between gap-3">
              <div className="flex-1 min-w-0">
                <CardTitle className="text-lg leading-tight mb-2 line-clamp-2">
                  <Link to={`/publications/${pubId}`} className="hover:text-primary transition-colors">
                    {title}
                  </Link>
                </CardTitle>
                <div className="flex items-center gap-2 text-xs text-muted-foreground ui-text mb-2">
                  <span className="truncate">{formatAuthors(authors, 2)}</span>
                  <span>•</span>
                  <span>{publishDate}</span>
                </div>
              </div>
              <CardAction>
                <button
                    className={cn(
                        "p-1.5 rounded-md transition-colors text-xs",
                        isInReadingList
                            ? "text-primary bg-primary/10 hover:bg-primary/20"
                            : "text-muted-foreground hover:text-foreground hover:bg-accent"
                    )}
                >
                  {isInReadingList ? <BookmarkCheck className="w-4 h-4" /> : <Bookmark className="w-4 h-4" />}
                </button>
              </CardAction>
            </div>
          </CardHeader>

          <CardFooter className="pt-3 justify-between">
            <div className="flex items-center gap-3 text-xs text-muted-foreground ui-text">
            <span className="flex items-center gap-1">
              <Eye className="w-3 h-3" />
              {views.toLocaleString()}
            </span>
              <span className="flex items-center gap-1">
              <Heart className="w-3 h-3" />
                {likes.toLocaleString()}
            </span>
            </div>
            <div className="flex items-center gap-1.5">
              {tags.slice(0, 3).map((tag, index) => (
                  <span
                      key={index}
                      className="ui-text px-1.5 py-0.5 bg-secondary/10 text-secondary text-xs rounded"
                  >
                {tag}
              </span>
              ))}
            </div>
          </CardFooter>
        </Card>
    )
  }

  return (
      <Card className={cn("hover:shadow-lg transition-all duration-200", className)} {...props}>
        <CardHeader>
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0">
              <CardTitle className="text-balance leading-tight mb-3">
                <Link to={`/publications/${pubId}`} className="hover:text-primary transition-colors">
                  {title}
                </Link>
              </CardTitle>
              <div className="flex items-center gap-3 text-sm text-muted-foreground ui-text mb-1">
                <span>{formatAuthors(authors)}</span>
                <span>•</span>
                <span>{publishDate}</span>
              </div>
            </div>
            <CardAction>
              <button
                  className={cn(
                      "p-2 rounded-lg transition-colors",
                      isInReadingList
                          ? "text-primary bg-primary/10 hover:bg-primary/20"
                          : "text-muted-foreground hover:text-foreground hover:bg-accent"
                  )}
              >
                {isInReadingList ? <BookmarkCheck className="w-5 h-5" /> : <Bookmark className="w-5 h-5" />}
              </button>
            </CardAction>
          </div>
        </CardHeader>

        {abstract && (
            <CardContent className="text-muted-foreground leading-relaxed -mt-2">
              {truncateText(abstract)}
            </CardContent>
        )}

        <CardFooter className="justify-between">
          <div className="flex items-center gap-2">
            {tags.slice(0, 3).map((tag, index) => (
                <span
                    key={index}
                    className="ui-text px-2 py-1 bg-secondary/10 text-secondary text-xs rounded-md"
                >
              {tag}
            </span>
            ))}
            {tags.length > 3 && (
                <span className="ui-text text-xs text-muted-foreground">+{tags.length - 3} more</span>
            )}
          </div>
          <div className="flex items-center gap-4 text-sm text-muted-foreground ui-text">
          <span className="flex items-center gap-1">
            <Eye className="w-4 h-4" />
            {views.toLocaleString()}
          </span>
            <span className="flex items-center gap-1">
            <Heart className="w-4 h-4" />
              {likes.toLocaleString()}
          </span>
          </div>
        </CardFooter>
      </Card>
  )
}
