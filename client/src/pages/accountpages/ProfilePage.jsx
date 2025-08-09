import { useEffect, useState } from "react"
import { useAuth } from "@/context/AuthContext.jsx"
import {
  Avatar,
  AvatarFallback,
  AvatarImage
} from "@/components/avatar.jsx"
import { Button } from "@/components/button.jsx"
import { Badge } from "@/components/badge.jsx"
import { toast } from "sonner"
import { userApi, authorRequestApi } from "@/lib/api.js"
import { Bookmark, Eye, Heart } from "lucide-react"
import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"

export default function ProfilePage() {
  const { user } = useAuth()
  const [stats, setStats] = useState({ read: 0, liked: 0, saved: 0 })
  const [interactions, setInteractions] = useState([])
  const [isStatsLoading, setIsStatsLoading] = useState(true)
  const [isInteractionLoading, setIsInteractionLoading] = useState(true)
  const [isPending, setIsPending] = useState(false)

  useEffect(() => {
    userApi.getStats()
    .then(setStats)
    .finally(() => setIsStatsLoading(false))

    userApi.getInteractions(5)
    .then(setInteractions)
    .finally(() => setIsInteractionLoading(false))

    authorRequestApi.getStatus().then((data) => {
      if (data.status === "PENDING") setIsPending(true)
    })
  }, [])

  if (!user) return null

  const isReader = user.role === "READER"
  const isAuthor = user.role === "AUTHOR"
  const isAdmin = user.isAdmin === true

  return (
      <section className="min-h-screen bg-[var(--color-background)] py-24 px-6">
        <div className="max-w-4xl mx-auto space-y-12">
          <div className="bg-[var(--color-surface)] border border-[var(--color-border)] rounded-2xl
          shadow-lg px-8 py-10 flex flex-col sm:flex-row items-center sm:items-start gap-8">
            <Avatar className="w-24 h-24 text-4xl shrink-0">
              <AvatarImage src="" alt="" />
              <AvatarFallback
                  firstName={user.firstName}
                  lastName={user.lastName}
                  className="text-4xl"
              />
            </Avatar>

            <div className="flex flex-col gap-3 w-full font-ui">
              <div className="flex flex-wrap items-center gap-3">
                <h2 className="text-2xl font-semibold leading-tight text-[var(--color-foreground)]">
                  {user.firstName} {user.lastName}
                </h2>

                {(isReader || isAuthor) && (
                    <Badge variant="secondary" className="text-xs px-2 py-0.5">
                      {isAuthor ? "Author" : "Reader"}
                    </Badge>
                )}

                {isAdmin && (
                    <Badge variant="destructive" className="text-xs px-2 py-0.5">
                      Admin
                    </Badge>
                )}
              </div>

              <div className="text-sm text-[var(--color-muted-foreground)] space-y-1">
                <p>@{user.username}</p>
                <p>{user.email}</p>
              </div>

              {!isAuthor && (
                  <Button
                      size="sm"
                      className="mt-3 w-fit"
                      onClick={handleAuthorRequest}
                      disabled={isPending}
                  >
                    {isPending ? "Request Pending..." : "Request Author Access"}
                  </Button>
              )}
            </div>
          </div>

          <div
              className={cn(
                  "bg-[var(--color-surface)] border border-[var(--color-border)] rounded-2xl p-8 font-ui "
                  + "transition-opacity duration-500 ease-in-out",
                  isStatsLoading ? "opacity-0 pointer-events-none" : "opacity-100"
              )}
          >
            <h3 className="text-xl font-semibold text-[var(--color-foreground)] mb-6">
              Your Stats
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
              <div className="space-y-1">
                <p className="text-4xl font-bold text-[var(--color-foreground)]">{stats.read}</p>
                <p className="text-sm text-[var(--color-muted-foreground)]">Read</p>
              </div>
              <div className="space-y-1">
                <p className="text-4xl font-bold text-[var(--color-foreground)]">{stats.liked}</p>
                <p className="text-sm text-[var(--color-muted-foreground)]">Liked</p>
              </div>
              <div className="space-y-1">
                <p className="text-4xl font-bold text-[var(--color-foreground)]">{stats.saved}</p>
                <p className="text-sm text-[var(--color-muted-foreground)]">Saved</p>
              </div>
            </div>
          </div>

          <div
              className={cn(
                  "bg-[var(--color-surface)] border border-[var(--color-border)] rounded-2xl p-8 font-ui "
                  + "transition-opacity duration-500 ease-in-out",
                  isInteractionLoading ? "opacity-0 pointer-events-none" : "opacity-100"
              )}
          >
            <h3 className="text-xl font-semibold text-[var(--color-foreground)] mb-6">
              Recent Interactions
            </h3>

            {interactions.length === 0 && !isInteractionLoading ? (
                <p className="text-sm text-muted-foreground text-center">
                  No recent activity.
                </p>
            ) : (
                <div className="space-y-4">
                  {interactions.map((i, idx) => (
                      <div key={idx} className="flex items-start gap-3">
                        <div className="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-full bg-[--color-primary]/10">
                          {getIcon(i.type)}
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-ui text-sm font-medium text-foreground">
                            <Link to={`/publications/${i.pubId}`} className="hover:underline">
                              {i.title}
                            </Link>
                          </p>
                          <p className="text-xs text-muted-foreground font-ui">
                            You {getVerb(i.type)} this paper.
                          </p>
                        </div>
                      </div>
                  ))}
                </div>
            )}
          </div>
        </div>
      </section>
  )

  async function handleAuthorRequest() {
    try {
      await authorRequestApi.submit()
      toast.success("Author request submitted!")
      setIsPending(true)
    } catch {
      toast.error("Failed to submit author request.")
    }
  }

  function getIcon(type) {
    const icons = {
      VIEW: <Eye className="h-4 w-4 text-[--color-primary]" />,
      LIKE: <Heart className="h-4 w-4 text-[--color-primary]" />,
      SAVE: <Bookmark className="h-4 w-4 text-[--color-primary]" />
    }
    return icons[type] || null
  }
}

function getVerb(type) {
  switch (type) {
    case "VIEW":
      return "viewed"
    case "LIKE":
      return "liked"
    case "SAVE":
      return "saved"
    default:
      return "interacted with"
  }
}