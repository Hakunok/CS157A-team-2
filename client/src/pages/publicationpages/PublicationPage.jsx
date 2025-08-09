import React from "react";
import { useParams } from "react-router-dom";
import { Eye, Heart, Bookmark, ExternalLink } from "lucide-react";
import * as Tooltip from "@radix-ui/react-tooltip";
import { publicationApi, collectionApi } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";
import LoadingOverlay from "@/components/LoadingOverlay.jsx";
import { KindBadge } from "@/components/KindBadge.jsx";
import { TopicBadge } from "@/components/TopicBadge.jsx";

import hljs from "highlight.js";
import "highlight.js/styles/tokyo-night-dark.css";

function toDate(input) {
  if (!input) return null;
  if (Array.isArray(input)) {
    const [y, m, d, h = 0, min = 0] = input;
    return new Date(y, m - 1, d, h, min);
  }
  return new Date(input);
}

const TooltipContent = React.forwardRef(({ children, ...props }, ref) => (
    <Tooltip.Portal>
      <Tooltip.Content
          ref={ref}
          side="top"
          align="center"
          className="z-50 rounded-md px-3 py-1.5 text-xs font-medium shadow-md bg-[var(--color-popover)]
          text-[var(--color-popover-foreground)] border border-[var(--color-border)] animate-in fade-in zoom-in"
          {...props}
      >
        {children}
        <Tooltip.Arrow className="fill-[var(--color-border)]" />
      </Tooltip.Content>
    </Tooltip.Portal>
));

export default function PublicationPage() {
  const { pubId } = useParams();
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = React.useState(true);
  const [publication, setPublication] = React.useState(null);
  const [liked, setLiked] = React.useState(false);
  const [saved, setSaved] = React.useState(false);
  const [isLiking, setIsLiking] = React.useState(false);
  const [isSaving, setIsSaving] = React.useState(false);
  const [showContent, setShowContent] = React.useState(false);
  const hasViewed = React.useRef(false);
  const contentRef = React.useRef(null);

  React.useEffect(() => {
    const fetchPublicationData = async () => {
      try {
        const pub = await publicationApi.getById(pubId);
        setPublication(pub);
        setLoading(false);

        if (!hasViewed.current) {
          publicationApi.view(pubId);
          hasViewed.current = true;
        }

        if (isAuthenticated) {
          const [{ liked }, { saved }] = await Promise.all([
            publicationApi.hasLiked(pubId),
            collectionApi.isSaved(pubId),
          ]);
          setLiked(liked);
          setSaved(saved);
        }

        setTimeout(() => setShowContent(true), 100);
      } catch (e) {
        console.error("Failed to fetch publication:", e);
        setLoading(false);
      }
    };

    fetchPublicationData();
  }, [pubId, isAuthenticated]);

  React.useEffect(() => {
    if (contentRef.current) {
      const codeBlocks = contentRef.current.querySelectorAll("pre code");
      codeBlocks.forEach((block) => {
        hljs.highlightElement(block);
      });
    }
  });

  const toggleLike = async () => {
    if (!isAuthenticated || isLiking) return;
    setIsLiking(true);
    try {
      if (!liked) {
        await publicationApi.like(pubId);
        setLiked(true);
      } else {
        await publicationApi.unlike(pubId);
        setLiked(false);
      }
    } catch (e) {
      console.error("Failed to toggle like:", e);
    } finally {
      setIsLiking(false);
    }
  };

  const toggleSave = async () => {
    if (!isAuthenticated || isSaving) return;
    setIsSaving(true);
    try {
      if (!saved) {
        await collectionApi.saveToDefault(pubId);
        setSaved(true);
      } else {
        await collectionApi.removeFromDefault(pubId);
        setSaved(false);
      }
    } catch (e) {
      console.error("Failed to toggle save:", e);
    } finally {
      setIsSaving(false);
    }
  };

  const formatDate = (dateInput) => {
    const date = toDate(dateInput);
    if (!date || isNaN(date.getTime())) return null;
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  if (loading || !publication)
    return <LoadingOverlay message="Loading publication..." />;

  const { title, content, doi, url, kind, publishedAt, authors, topics, viewCount, likeCount } = publication;
  const publicationDate = formatDate(publishedAt);

  return (
      <div className="min-h-screen bg-background py-16 px-6">
        <div
            ref={contentRef}
            className={cn(
                "max-w-4xl mx-auto space-y-6 transition-opacity duration-500 ease-in-out",
                showContent ? "opacity-100" : "opacity-0 pointer-events-none"
            )}
        >
          <div className="space-y-3">
            <div className="flex items-center gap-4 flex-wrap">
              <KindBadge kind={kind} />
              <div className="flex gap-2 items-center flex-wrap">
                {topics.map((t) => (
                    <TopicBadge key={t.topicId} topic={t} />
                ))}
              </div>
            </div>
            <h1 className="publication-title font-content">{title}</h1>
          </div>

          <div className="text-sm font-ui text-muted-foreground flex items-center flex-wrap gap-x-4 gap-y-2">
            <div className="flex gap-2 items-center">
              {authors.map((a) => (
                  <span key={a.personId}>
                {a.firstName} {a.lastName}
              </span>
              ))}
            </div>
            {publicationDate && (
                <>
                  <span className="text-muted-foreground/50">•</span>
                  <span>{publicationDate}</span>
                </>
            )}
          </div>

          <Tooltip.Provider>
            <div className="flex items-center gap-4 text-sm font-ui border-t border-b border-border/80 py-2">
              <div className="flex items-center gap-4">
              <span className="flex items-center gap-1.5 text-muted-foreground">
                <Eye className="w-4 h-4" /> {viewCount}
              </span>
                <span className="flex items-center gap-1.5 text-muted-foreground">
                <Heart className="w-4 h-4" /> {likeCount}
              </span>
              </div>

              <div className="h-6 border-l border-border/80 mx-2"></div>

              <div className="flex items-center gap-2">
                {/* like button */}
                <Tooltip.Root>
                  <Tooltip.Trigger asChild>
                    <button
                        onClick={toggleLike}
                        disabled={!isAuthenticated || isLiking}
                        className={cn(
                            "p-2 rounded-md transition-colors disabled:opacity-50",
                            liked
                                ? "text-[var(--color-destructive)] bg-[var(--color-destructive)]/10 "
                                + "hover:bg-[var(--color-destructive)]/20"
                                : "text-muted-foreground hover:bg-muted/60"
                        )}
                    >
                      <Heart
                          className="w-5 h-5"
                          fill={liked ? "currentColor" : "none"}
                      />
                    </button>
                  </Tooltip.Trigger>
                  <TooltipContent>{liked ? "Unlike" : "Like"}</TooltipContent>
                </Tooltip.Root>

                {/* save button */}
                <Tooltip.Root>
                  <Tooltip.Trigger asChild>
                    <button
                        onClick={toggleSave}
                        disabled={!isAuthenticated || isSaving}
                        className={cn(
                            "p-2 rounded-md transition-colors disabled:opacity-50",
                            saved
                                ? "text-[var(--color-primary)] bg-[var(--color-primary)]/10 hover:bg-[var(--color-primary)]/20"
                                : "text-muted-foreground hover:bg-muted/60"
                        )}
                    >
                      <Bookmark
                          className="w-5 h-5"
                          fill={saved ? "currentColor" : "none"}
                      />
                    </button>
                  </Tooltip.Trigger>
                  <TooltipContent>{saved ? "Unsave" : "Save"}</TooltipContent>
                </Tooltip.Root>

                {/* doi button */}
                {doi && (
                    <Tooltip.Root>
                      <Tooltip.Trigger asChild>
                        <a
                            href={doi}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-md transition-all
                            duration-200 ease-in-out text-[var(--secondary-400)] bg-transparent border border-[var(--secondary-600)]
                            hover:text-[var(--secondary-300)] hover:border-[var(--secondary-400)] hover:bg-[var(--secondary-600)]/5"
                            style={{
                              textDecoration: "underline",
                              textUnderlineOffset: "0.15em",
                              textDecorationColor: "var(--secondary-600)",
                              textDecorationThickness: "1px",
                            }}
                        >
                          DOI
                        </a>
                      </Tooltip.Trigger>
                      <TooltipContent>Open DOI</TooltipContent>
                    </Tooltip.Root>
                )}

                {/* pdf button */}
                {url && (
                    <Tooltip.Root>
                      <Tooltip.Trigger asChild>
                        <a
                            href={url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs
                            font-medium rounded-md transition-all duration-200 ease-in-out text-[var(--secondary-400)]
                            bg-transparent border border-[var(--secondary-600)] hover:text-[var(--secondary-300)]
                            hover:border-[var(--secondary-400)] hover:bg-[var(--secondary-600)]/5"
                            style={{
                              textDecoration: "underline",
                              textUnderlineOffset: "0.15em",
                              textDecorationColor: "var(--secondary-600)",
                              textDecorationThickness: "1px",
                            }}
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                          PDF
                        </a>
                      </Tooltip.Trigger>
                      <TooltipContent>Open PDF</TooltipContent>
                    </Tooltip.Root>
                )}
              </div>
            </div>
          </Tooltip.Provider>

          {/* publication content */}
          {kind === "PAPER" && (
              <>
                <div className="prose text-reading max-w-none">
                  <h2>Abstract</h2>
                </div>
                <div
                    className="prose text-reading max-w-none"
                    dangerouslySetInnerHTML={{ __html: content }}
                />
              </>
          )}

          {(kind === "BLOG" || kind === "ARTICLE") && (
              <div
                  className="prose text-reading max-w-none"
                  dangerouslySetInnerHTML={{ __html: content }}
              />
          )}
        </div>
      </div>
  );
}
