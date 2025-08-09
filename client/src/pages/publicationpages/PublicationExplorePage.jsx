import React from "react";
import { PublicationCard } from "@/components/PublicationCard.jsx";
import { TopicBadge } from "@/components/TopicBadge.jsx";
import { publicationApi, topicApi } from "@/lib/api";
import { Button } from "@/components/button.jsx";
import { cn } from "@/lib/utils";

export default function PublicationExplorePage({ kinds }) {
  const [publications, setPublications] = React.useState([]);
  const [page, setPage] = React.useState(1);
  const [isInitialLoading, setIsInitialLoading] = React.useState(true);
  const [isRefreshing, setIsRefreshing] = React.useState(false);
  const [hasMore, setHasMore] = React.useState(true);
  const [allTopics, setAllTopics] = React.useState([]);
  const [selectedTopicIds, setSelectedTopicIds] = React.useState(new Set());
  const [showFilters, setShowFilters] = React.useState(false);
  const [showContent, setShowContent] = React.useState(false);
  const selectedTopicsKey = React.useMemo(
      () => Array.from(selectedTopicIds).sort().join(","),
      [selectedTopicIds]
  );
  const isAll = !kinds || kinds.length === 0;
  const isOnlyPapers = kinds?.length === 1 && kinds[0] === "PAPER";
  const pageTitle = isAll ? "All Publications" : isOnlyPapers ? "Papers" : "Blogs & Articles";
  const pageDescription = isAll
      ? "Discover new publications from across the community"
      : isOnlyPapers
          ? "Discover new papers from the community"
          : "Discover new blog posts and articles from the community";

  React.useEffect(() => {
    topicApi
    .getAll()
    .then((data) => {
      setAllTopics(data);
      setShowFilters(true);
    })
    .catch((err) => console.error("Failed to fetch topics", err));
  }, []);

  React.useEffect(() => {
    setIsInitialLoading(true);
    setIsRefreshing(true);
    setShowContent(false);

    const timeout = setTimeout(() => {
      setShowContent(true);
    }, 100);

    loadPublications(1);

    return () => clearTimeout(timeout);
  }, [kinds?.join(","), selectedTopicsKey]);

  const loadPublications = async (pageToLoad) => {
    try {
      const res = await publicationApi.getRecommendations({
        kinds,
        topicIds: Array.from(selectedTopicIds),
        page: pageToLoad,
      });

      if (pageToLoad === 1) {
        setPublications(res);
      } else {
        setPublications((prev) => [...prev, ...res]);
      }

      setPage(pageToLoad);
      setHasMore(res.length >= 10);
    } catch (err) {
      console.error("Failed to load publications", err);
    } finally {
      setIsInitialLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleLoadMore = () => {
    setIsRefreshing(true);
    loadPublications(page + 1);
  };

  const handleTopicToggle = (topicId) => {
    setSelectedTopicIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(topicId)) {
        newSet.delete(topicId);
      } else {
        newSet.add(topicId);
      }
      return newSet;
    });
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

          {/* topic filters */}
          <div className="space-y-4">
            <h3 className="font-medium font-ui text-foreground">Filter by Topic</h3>
            <div
                className={cn(
                    "flex flex-wrap gap-2",
                    "transition-opacity duration-500 ease-in-out",
                    showFilters ? "opacity-100" : "opacity-0 pointer-events-none"
                )}
            >
              {allTopics.map((topic) => (
                  <TopicBadge
                      key={topic.topicId}
                      topic={topic}
                      isSelected={selectedTopicIds.has(topic.topicId)}
                      onClick={() => handleTopicToggle(topic.topicId)}
                  />
              ))}
            </div>
          </div>

          {/* publication grid */}
          <div
              className={cn(
                  "grid gap-4 items-start min-h-[400px]",
                  "transition-opacity duration-500 ease-in-out",
                  showContent ? "opacity-100" : "opacity-0",
                  isRefreshing && "opacity-50 pointer-events-none"
              )}
          >
            {publications.length === 0 ? (
                !isInitialLoading &&
                !isRefreshing && (
                    <p className="text-muted-foreground font-content text-center py-8 col-span-full">
                      No publications found for the selected filters.
                    </p>
                )
            ) : (
                publications.map((pub) => (
                    <PublicationCard key={pub.pubId} publication={pub} variant="published" />
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