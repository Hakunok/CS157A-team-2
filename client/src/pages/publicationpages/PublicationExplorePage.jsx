import React from "react"
import { useSearchParams } from "react-router-dom"
import { PublicationCard } from "@/components/ui/PublicationCard"
import { publicationApi } from "@/lib/api"
import { Button } from "@/components/ui/button"
import LoadingOverlay from "@/components/shared/LoadingOverlay"

const kindDisplay = {
  PAPER: "Papers",
  BLOG: "Blogs",
  ARTICLE: "Articles"
}

export default function PublicationExplorePage({ kind }) {
  const [publications, setPublications] = React.useState([])
  const [page, setPage] = React.useState(1)
  const [loading, setLoading] = React.useState(true)
  const [hasMore, setHasMore] = React.useState(true)

  React.useEffect(() => {
    loadPublications(1)
  }, [kind])

  const loadPublications = async (pageToLoad) => {
    try {
      const res = await publicationApi.getRecommendations({ kind, page: pageToLoad })
      if (pageToLoad === 1) {
        setPublications(res)
      } else {
        setPublications((prev) => [...prev, ...res])
      }
      setPage(pageToLoad)
      setHasMore(res.length >= 10)
    } catch (err) {
      console.error("Failed to load publications", err)
    } finally {
      setLoading(false)
    }
  }

  const handleLoadMore = () => {
    loadPublications(page + 1)
  }

  return (
      <section className="min-h-screen bg-background py-16 px-6">
        <div className="max-w-5xl mx-auto space-y-12">
          <div>
            <h1 className="text-4xl font-semibold font-ui text-foreground mb-2">
              Explore {kindDisplay[kind]}
            </h1>
            <p className="text-sm text-muted-foreground font-ui">
              Discover new {kindDisplay[kind].toLowerCase()} from the community
            </p>
          </div>

          {loading ? (
              <LoadingOverlay message={`Loading ${kindDisplay[kind].toLowerCase()}...`} />
          ) : publications.length === 0 ? (
              <p className="text-muted-foreground font-content">No publications found.</p>
          ) : (
              <>
                <div className="grid gap-4">
                  {publications.map((pub) => (
                      <PublicationCard
                          key={pub.pubId}
                          publication={pub}
                          variant="published"
                      />
                  ))}
                </div>
                {hasMore && (
                    <div className="text-center pt-6">
                      <Button variant="ghost" onClick={handleLoadMore}>
                        Load More
                      </Button>
                    </div>
                )}
              </>
          )}
        </div>
      </section>
  )
}
