import React, { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import {
  BarChart2, FileText, Eye, Heart,
  Users, BookOpen, Bookmark, ArrowRight
} from "lucide-react"

import { useAuth } from "@/context/AuthContext"
import { userApi, publicationApi } from "@/lib/api"
import { Button } from "@/components/button.jsx"
import { PublicationCard } from "@/components/PublicationCard.jsx"
import { cn } from "@/lib/utils"

export default function HomePage() {
  const { user, loading } = useAuth()

  if (loading) {
    return null
  }

  return (
      <div className="min-h-screen mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-4 lg:gap-16">
          <main className="lg:col-span-3 space-y-16">
            {!user ? <GuestHero /> : <WelcomeHero user={user} />}
            <FeaturedPublications user={user} />
          </main>

          <aside className="space-y-8">
            <PlatformStats />
            {user && (
                <>
                  <UserStats />
                  <RecentInteractions />
                </>
            )}
          </aside>
        </div>
      </div>
  )
}

function GuestHero() {
  return (
      <section className="text-center space-y-8 pb-6">
        <div className="max-w-3xl mx-auto space-y-4">
          <h1 className="text-5xl lg:text-6xl font-extrabold font-ui tracking-tight leading-tight text-foreground">
            Curated Archive of AI
          </h1>
          <p className="text-lg lg:text-xl text-muted-foreground font-ui">
            Browse some cool papers or articles or blogs or collections or not.
          </p>
        </div>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button asChild size="lg">
            <Link to="/publications">Browse Publications</Link>
          </Button>
          <Button asChild size="lg" variant="outline">
            <Link to="/signup">Create Account</Link>
          </Button>
        </div>
      </section>
  )
}

function WelcomeHero({ user }) {
  return (
      <section className="space-y-4 pb-4">
        <h2 className="text-3xl font-semibold font-ui text-foreground">
          Welcome back, <span className="text-[var(--color-primary)]/90">{user.firstName}</span> 👋
        </h2>
        <p className="text-muted-foreground font-ui text-sm">
          Let’s continue your journey through the latest in artificial intelligence.
        </p>
      </section>
  )
}

function FeaturedPublications({ user }) {
  const [publications, setPublications] = useState([])
  const [isInitialLoading, setIsInitialLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      setIsRefreshing(true)
      try {
        const res = await publicationApi.getRecommendations({ pageSize: 6 })
        setPublications(res || [])
      } catch (e) {
        console.error("Failed to fetch publications", e)
      } finally {
        setIsInitialLoading(false)
        setIsRefreshing(false)
      }
    }
    fetchData()
  }, [user])

  const title = user ? "Recommended For You" : "Featured Publications"

  return (
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold font-ui text-foreground">{title}</h2>
          <Button asChild variant="ghost" className="text-[--color-primary]">
            <Link to="/publications">
              Explore All <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
        </div>

        <div
            className={cn(
                "grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3 min-h-[13rem]",
                "transition-all duration-500 ease-in-out",
                isInitialLoading ? "opacity-0" : "opacity-100",
                isRefreshing && "opacity-50 pointer-events-none"
            )}
        >
          {publications.length === 0 && !isInitialLoading && !isRefreshing ? (
              <p className="col-span-full py-8 text-center font-content text-muted-foreground">
                No publications found.
              </p>
          ) : (
              publications.map((pub) => (
                  <PublicationCard key={pub.pubId} publication={pub} variant="compact" />
              ))
          )}
        </div>
      </section>
  )
}

function PlatformStats() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    userApi.getPlatformStats()
    .then(setStats)
    .catch(console.error)
    .finally(() => setLoading(false))
  }, [])

  return (
      <section
          className={cn(
              "rounded-xl border border-[--color-border] bg-[--color-surface] p-6 transition-opacity duration-500 ease-in-out",
              loading ? "opacity-0 pointer-events-none" : "opacity-100"
          )}
      >
        <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
          <BarChart2 className="h-5 w-5 text-[--color-primary]" />
          Platform Stats
        </h3>
        <div className="space-y-3 text-sm font-ui">
          <StatRow icon={<FileText className="h-4 w-4" />} label="Publications" value={stats?.publications} />
          <StatRow icon={<Eye className="h-4 w-4" />} label="Total Views" value={stats?.views} />
          <StatRow icon={<Heart className="h-4 w-4" />} label="Total Likes" value={stats?.likes} />
        </div>
      </section>
  )
}

function UserStats() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    userApi.getStats()
    .then(setStats)
    .catch(console.error)
    .finally(() => setLoading(false))
  }, [])

  return (
      <section
          className={cn(
              "rounded-xl border border-[--color-border] bg-[--color-surface] p-6 transition-opacity duration-500 ease-in-out",
              loading ? "opacity-0 pointer-events-none" : "opacity-100"
          )}
      >
        <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
          <Users className="h-5 w-5 text-[--color-primary]" />
          Your Stats
        </h3>
        <div className="space-y-3 text-sm font-ui">
          <StatRow label="Read" value={stats?.read} />
          <StatRow label="Liked" value={stats?.liked} />
          <StatRow label="Saved" value={stats?.saved} />
        </div>
      </section>
  )
}

function RecentInteractions() {
  const [interactions, setInteractions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    userApi.getInteractions(3)
    .then(setInteractions)
    .catch(console.error)
    .finally(() => setLoading(false))
  }, [])

  const icons = {
    VIEW: <Eye className="h-4 w-4 text-[--color-primary]" />,
    LIKE: <Heart className="h-4 w-4 text-[--color-primary]" />,
    SAVE: <Bookmark className="h-4 w-4 text-[--color-primary]" />
  }

  return (
      <section
          className={cn(
              "rounded-xl border border-[--color-border] bg-[--color-surface] p-6 transition-opacity duration-500 ease-in-out",
              loading ? "opacity-0 pointer-events-none" : "opacity-100"
          )}
      >
        <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
          <BookOpen className="h-5 w-5 text-[--color-primary]" />
          Recent Interactions
        </h3>
        <div className="space-y-4">
          {interactions.length === 0 ? (
              <p className="text-sm text-muted-foreground">No recent activity.</p>
          ) : (
              interactions.map((i, idx) => (
                  <div key={idx} className="flex items-start gap-3">
                    <div className="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-full bg-[--color-primary]/10">
                      {icons[i.type]}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-ui text-sm font-medium text-foreground">
                        <Link to={`/publications/${i.pubId}`} className="hover:underline">
                          {i.title}
                        </Link>
                      </p>
                      <p className="text-xs text-muted-foreground font-ui">
                        You {getVerb(i.type)} this publication.
                      </p>
                    </div>
                  </div>
              ))
          )}
        </div>
      </section>
  )
}

function StatRow({ icon = null, label, value }) {
  return (
      <div className="flex items-center justify-between">
      <span className="flex items-center gap-2 text-muted-foreground">
        {icon}
        {label}
      </span>
        <span className="font-medium">{value?.toLocaleString?.() ?? "N/A"}</span>
      </div>
  )
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