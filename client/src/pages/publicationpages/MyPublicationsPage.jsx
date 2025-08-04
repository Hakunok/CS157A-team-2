import React from "react"
import { useNavigate } from "react-router-dom"
import { FileText, Eye, Plus } from "lucide-react"

import { useAuth } from "@/context/AuthContext.jsx"
import { publicationApi } from "@/lib/api.js"
import { PublicationCard } from "@/components/ui/PublicationCard.jsx"
import LoadingOverlay from "@/components/shared/LoadingOverlay.jsx"
import { Button } from "@/components/ui/button.jsx"
import { Card, CardContent } from "@/components/ui/card.jsx"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs.jsx"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog.jsx"

export default function MyPublicationsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [tab, setTab] = React.useState("drafts")
  const [loading, setLoading] = React.useState(true)
  const [showKindDialog, setShowKindDialog] = React.useState(false)
  const [publications, setPublications] = React.useState({ drafts: [], published: [] })

  React.useEffect(() => {
    if (!user) return

    const fetchPublications = async () => {
      try {
        const all = await publicationApi.getMyPublications()
        const drafts = all.filter((pub) => !pub.publishedAt)
        const published = all.filter((pub) => pub.publishedAt)
        setPublications({ drafts, published })
      } catch (err) {
        console.error("Failed to fetch publications:", err)
      } finally {
        setLoading(false)
      }
    }

    fetchPublications()
  }, [user])

  if (!user) return null
  if (loading) return <LoadingOverlay message="Loading your publications..." />

  return (
      <section className="min-h-screen bg-background py-16 px-6">
        <div className="max-w-5xl mx-auto space-y-12">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-semibold font-ui text-foreground mb-2">
                Your Publications
              </h1>
              <p className="text-sm text-muted-foreground font-ui">
                Manage your papers, blogs, and articles
              </p>
            </div>
            <Button onClick={() => setShowKindDialog(true)}>
              <Plus className="w-4 h-4 mr-2" />
              New Publication
            </Button>
          </div>

          {/* Tabs */}
          <Tabs value={tab} onValueChange={setTab}>
            <TabsList className="mb-6">
              <TabsTrigger value="drafts" className="font-ui">
                Drafts ({publications.drafts.length})
              </TabsTrigger>
              <TabsTrigger value="published" className="font-ui">
                Published ({publications.published.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="drafts">
              {publications.drafts.length === 0 ? (
                  <EmptyState
                      icon={<FileText className="w-12 h-12" />}
                      title="No drafts yet..."
                      description="What are you doing???"
                      onCreate={() => setShowKindDialog(true)}
                  />
              ) : (
                  <div className="grid gap-4 grid-cols-1 md:grid-cols-2">
                    {publications.drafts.map((pub) => (
                        <PublicationCard
                            key={pub.pubId}
                            publication={pub}
                            variant="draft"
                            onEdit={() => navigate(`/my-publications/${pub.pubId}/edit`)}
                            onPublish={() => navigate(`/my-publications/${pub.pubId}/publish`)}
                        />
                    ))}
                  </div>
              )}
            </TabsContent>

            <TabsContent value="published">
              {publications.published.length === 0 ? (
                  <EmptyState
                      icon={<Eye className="w-12 h-12" />}
                      title="How about... idk... publishing something?"
                      description="JUST DOOOOOOOO IT!!!!!!!!!!!!!!"
                  />
              ) : (
                  <div className="grid gap-4">
                    {publications.published.map((pub) => (
                        <PublicationCard
                            key={pub.pubId}
                            publication={pub}
                            variant="published"
                        />
                    ))}
                  </div>
              )}
            </TabsContent>
          </Tabs>
        </div>

        {/* Dialog for selecting publication kind */}
        <Dialog open={showKindDialog} onOpenChange={setShowKindDialog}>
          <DialogContent className="sm:max-w-sm">
            <DialogHeader>
              <DialogTitle className="font-ui text-base">
                Choose publication type
              </DialogTitle>
            </DialogHeader>
            <div className="grid gap-3 mt-2">
              {[
                { kind: "PAPER", label: "Paper" },
                { kind: "BLOG", label: "Blog" },
                { kind: "ARTICLE", label: "Article" },
              ].map(({ kind, label }) => (
                  <Button
                      key={kind}
                      variant="outline"
                      className="w-full font-ui"
                      onClick={() => {
                        navigate(`/my-publications/new?kind=${kind}`)
                        setShowKindDialog(false)
                      }}
                  >
                    {label}
                  </Button>
              ))}
            </div>
          </DialogContent>
        </Dialog>
      </section>
  )
}

function EmptyState({ icon, title, description }) {
  return (
      <Card>
        <CardContent className="p-12 text-center">
          <div className="text-muted-foreground mb-4">{icon}</div>
          <h3 className="text-lg font-semibold font-ui mb-2">{title}</h3>
          <p className="text-sm text-muted-foreground font-ui">{description}</p>
        </CardContent>
      </Card>
  )
}
