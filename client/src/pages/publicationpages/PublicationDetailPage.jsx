import React from "react";
import { useParams } from "react-router-dom";
import { Eye, Heart, Bookmark, BookmarkCheck, ExternalLink } from "lucide-react";
import { publicationApi, collectionApi } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import LoadingOverlay from "@/components/shared/LoadingOverlay";
import { KindBadge } from "@/components/ui/KindBadge";
import { TopicBadge } from "@/components/ui/TopicBadge";
import { Button } from "@/components/ui/button";

// Import Highlight.js and a theme
import hljs from "highlight.js";
import 'highlight.js/styles/tokyo-night-dark.css';

// Helper function to handle different date formats
function toDate(input) {
  if (!input) return null;
  if (Array.isArray(input)) {
    const [y, m, d, h = 0, min = 0] = input;
    return new Date(y, m - 1, d, h, min);
  }
  return new Date(input);
}

export default function PublicationDetailPage() {
  const { pubId } = useParams();
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = React.useState(true);
  const [publication, setPublication] = React.useState(null);
  const [liked, setLiked] = React.useState(false);
  const [saved, setSaved] = React.useState(false);

  const hasViewed = React.useRef(false);
  const contentRef = React.useRef(null);

  React.useEffect(() => {
    const fetch = async () => {
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
            collectionApi.isSaved(pubId)
          ]);
          setLiked(liked);
          setSaved(saved);
        }
      } catch (e) {
        console.error("Failed to fetch publication:", e);
        setLoading(false);
      }
    };

    fetch();
  }, [pubId, isAuthenticated]);

  React.useEffect(() => {
    if (contentRef.current) {
      const codeBlocks = contentRef.current.querySelectorAll('pre code');
      codeBlocks.forEach((block) => {
        hljs.highlightElement(block);
      });
    }
  });

  const toggleLike = async () => {
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
    }
  };

  const toggleSave = async () => {
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
    }
  };

  // Updated helper function to format the date string into a readable format.
  const formatDate = (dateInput) => {
    const date = toDate(dateInput);
    if (!date || isNaN(date.getTime())) return null;
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (loading || !publication) return <LoadingOverlay message="Loading publication..." />;

  const {
    title,
    content,
    doi,
    url,
    kind,
    publishedAt, // Make sure to get the date
    authors,
    topics,
    viewCount,
    likeCount
  } = publication;

  const publicationDate = formatDate(publishedAt);

  return (
      <div className="min-h-screen bg-background py-16 px-6">
        <div ref={contentRef} className="max-w-4xl mx-auto space-y-6">
          <div className="space-y-3">
            {/* MOVED: Kind and Topic badges are now on the same line */}
            <div className="flex items-center gap-4 flex-wrap">
              <KindBadge kind={kind} />
              <div className="flex gap-2 items-center flex-wrap">
                {topics.map((t) => <TopicBadge key={t.topicId} topic={t} />)}
              </div>
            </div>
            <h1 className="publication-title font-content">{title}</h1>
          </div>

          {/* ADDED: Publication date is now next to the authors */}
          <div className="text-sm font-ui text-muted-foreground flex items-center flex-wrap gap-x-4 gap-y-2">
            <div className="flex gap-2 items-center">
              {authors.map((a) => <span key={a.personId}>{a.firstName} {a.lastName}</span>)}
            </div>
            {publicationDate && (
                <>
                  <span className="text-muted-foreground/50">•</span>
                  <span>{publicationDate}</span>
                </>
            )}
          </div>

          <div className="flex gap-4 items-center text-sm">
            <div className="flex items-center gap-1 text-muted-foreground">
              <Eye className="w-4 h-4" /> {viewCount}
            </div>
            <div className="flex items-center gap-1 text-muted-foreground">
              <Heart className="w-4 h-4" /> {likeCount}
            </div>
            <Button
                size="icon"
                variant="ghost"
                onClick={toggleLike}
                disabled={!isAuthenticated}
                className="text-[var(--color-primary)]"
            >
              <Heart className="w-5 h-5" fill={liked ? "currentColor" : "none"} />
            </Button>
            <Button
                size="icon"
                variant="ghost"
                onClick={toggleSave}
                disabled={!isAuthenticated}
                className="text-[var(--color-primary)]"
            >
              {saved ? <BookmarkCheck className="w-5 h-5" /> : <Bookmark className="w-5 h-5" />}
            </Button>
          </div>

          {kind === "PAPER" && (
              <>
                <div className="prose text-reading max-w-none">
                  <h2>Abstract</h2>
                </div>
                <div
                    className="prose text-reading max-w-none"
                    dangerouslySetInnerHTML={{ __html: content }}
                />
                <div className="flex gap-4 mt-4">
                  {doi && (
                      <Button asChild variant="outline">
                        <a href={doi} target="_blank" rel="noopener noreferrer">
                          DOI <ExternalLink className="w-4 h-4 ml-2" />
                        </a>
                      </Button>
                  )}
                  {url && (
                      <Button asChild variant="outline">
                        <a href={url} target="_blank" rel="noopener noreferrer">
                          PDF <ExternalLink className="w-4 h-4 ml-2" />
                        </a>
                      </Button>
                  )}
                </div>
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