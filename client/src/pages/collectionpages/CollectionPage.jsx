import React from "react";
import { useParams } from "react-router-dom";
import { collectionApi } from "@/lib/api";
import { PublicationCard } from "@/components/PublicationCard.jsx";
import LoadingOverlay from "@/components/LoadingOverlay.jsx";
import { KindBadge } from "@/components/KindBadge.jsx";
import { VisibilityBadge } from "@/components/VisibilityBadge.jsx";
import { cn } from "@/lib/utils";
import hljs from "highlight.js";
import "highlight.js/styles/tokyo-night-dark.css";

export default function CollectionPage() {
  const { collectionId } = useParams();
  const [loading, setLoading] = React.useState(true);
  const [collection, setCollection] = React.useState(null);
  const [showContent, setShowContent] = React.useState(false);

  const contentRef = React.useRef(null);

  React.useEffect(() => {
    if (showContent && contentRef.current) {
      const codeBlocks = contentRef.current.querySelectorAll("pre code");
      codeBlocks.forEach((block) => {
        hljs.highlightElement(block);
      });
    }
  }, [showContent]);

  React.useEffect(() => {
    const fetchCollection = async () => {
      try {
        const data = await collectionApi.getById(collectionId);
        setCollection(data);
        setLoading(false);
        setTimeout(() => setShowContent(true), 100);
      } catch (e) {
        console.error("Failed to fetch collection:", e);
        setLoading(false);
      }
    };

    fetchCollection();
  }, [collectionId]);

  if (loading || !collection)
    return <LoadingOverlay message="Loading collection..." />;

  const { title, description, publications, isPublic } = collection;

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
              <KindBadge kind="COLLECTION" />
              <VisibilityBadge isPublic={isPublic} />
            </div>
            <h1 className="publication-title font-content">{title}</h1>
          </div>

          {/* collection description */}
          {description && (
              <div
                  className="prose text-reading max-w-none"
                  dangerouslySetInnerHTML={{ __html: description }}
              />
          )}

          {/* publication grid */}
          <div className="space-y-4">
            <h2 className="text-lg font-ui font-medium">Publications:</h2>
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
              {publications.length === 0 ? (
                  <p className="col-span-full text-muted-foreground font-content text-center py-8">
                    This collection has no publications yet.
                  </p>
              ) : (
                  publications.map((pub) => (
                      <PublicationCard
                          key={pub.pubId}
                          publication={pub}
                          variant="compact"
                      />
                  ))
              )}
            </div>
          </div>
        </div>
      </div>
  );
}