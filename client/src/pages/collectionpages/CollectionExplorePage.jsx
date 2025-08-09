import React from "react";
import { CollectionCard } from "@/components/CollectionCard.jsx";
import { collectionApi } from "@/lib/api";
import { Button } from "@/components/button.jsx";
import { cn } from "@/lib/utils";

export default function CollectionExplorePage() {
  const [collections, setCollections] = React.useState([]);
  const [page, setPage] = React.useState(1);
  const [isInitialLoading, setIsInitialLoading] = React.useState(true);
  const [isRefreshing, setIsRefreshing] = React.useState(false);
  const [hasMore, setHasMore] = React.useState(true);

  const [showContent, setShowContent] = React.useState(false);

  const pageTitle = "All Collections";
  const pageDescription = "Discover curated collections from across the community";

  React.useEffect(() => {
    setIsInitialLoading(true);
    setIsRefreshing(true);
    setShowContent(false);

    const timeout = setTimeout(() => {
      setShowContent(true);
    }, 100);

    loadCollections(1);

    return () => clearTimeout(timeout);
  }, []);

  const loadCollections = async (pageToLoad) => {
    try {
      const res = await collectionApi.getRecommendations({
        page: pageToLoad,
      });

      if (pageToLoad === 1) {
        setCollections(res);
      } else {
        setCollections((prev) => [...prev, ...res]);
      }

      setPage(pageToLoad);
      setHasMore(res.length >= 10);
    } catch (err) {
      console.error("Failed to load collections", err);
    } finally {
      setIsInitialLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleLoadMore = () => {
    setIsRefreshing(true);
    loadCollections(page + 1);
  };

  return (
      <section className="min-h-screen bg-background py-16 px-6">
        <div className="max-w-5xl mx-auto space-y-12">
          {/* page header */}
          <div>
            <h1 className="text-4xl font-semibold font-ui text-foreground mb-2">
              Explore {pageTitle}
            </h1>
            <p className="text-sm text-muted-foreground font-ui">{pageDescription}</p>
          </div>

          {/* collection grid */}
          <div
              className={cn(
                  "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-x-4 gap-y-4 mt-4",
                  "transition-opacity duration-500 ease-in-out",
                  showContent ? "opacity-100" : "opacity-0",
                  isRefreshing && "opacity-50 pointer-events-none"
              )}
          >
            {collections.length === 0 ? (
                !isInitialLoading &&
                !isRefreshing && (
                    <p className="text-muted-foreground font-content text-center py-8 col-span-full">
                      No collections found.
                    </p>
                )
            ) : (
                collections.map((collection) => (
                    <CollectionCard
                        key={collection.collectionId}
                        collection={collection}
                    />
                ))
            )}
          </div>

          {/* load more button */}
          {!isInitialLoading && !isRefreshing && hasMore && (
              <div className="text-center pt-6">
                <Button variant="ghost" onClick={handleLoadMore}>
                  Load More
                </Button>
              </div>
          )}
        </div>
      </section>
  );
}
