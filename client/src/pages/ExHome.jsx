import { useEffect, useState } from "react"
import { HeroSection } from "@/components/home/HeroSection"
import { HomeSidebar } from "@/components/home/HomeSidebar"
import { PublicationCard } from "@/components/home/PublicationCard"

// Dummy data — replace with real REST call
const featuredPublications = [
  {
    pubId: 1,
    title: "Attention Is All You Need",
    authors: ["Vaswani et al."],
    publishDate: "2017",
    views: 2400,
    likes: 300,
    tags: ["Transformers", "NLP"],
    abstract: "We propose the Transformer, a novel architecture...",
    isInReadingList: true
  },
  {
    pubId: 2,
    title: "Constitutional AI",
    authors: ["Bai et al."],
    publishDate: "2022",
    views: 1800,
    likes: 220,
    tags: ["AI Safety", "RLHF"],
    abstract: "We propose Constitutional AI to improve alignment...",
    isInReadingList: false
  }
]

const trendingTopics = [
  { name: "Multimodal AI", count: 42 },
  { name: "AI Safety", count: 38 },
  { name: "Diffusion Models", count: 35 }
]

const recentActivity = [
  { type: "saved", title: "Self-Rewarded Agents", time: "2h ago" },
  { type: "read", title: "Prompt Tuning for LLMs", time: "4h ago" },
  { type: "liked", title: "Scaling Vision Transformers", time: "Yesterday" }
]

const loggedInStats = { read: 127, liked: 43, saved: 18 }
const guestStats = { published: 876, read: 15230, liked: 3490 }

export default function HomePage() {
  const [isLoggedIn, setIsLoggedIn] = useState(true)
  const user = { fullName: "John Doe" }

  // Fetch featured/recommended publications from REST API here
  const [publications, setPublications] = useState(featuredPublications)

  // Optionally: fetch publications with useEffect
  /*
  useEffect(() => {
    fetch("/api/publications/recommended")
      .then((res) => res.json())
      .then(setPublications)
  }, [])
  */

  return (
      <div className="min-h-screen bg-background">
        <div className="max-w-7xl mx-auto px-6 py-8">
          {/* Hero */}
          <HeroSection isLoggedIn={isLoggedIn} user={user} />

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 mt-8">
            {/* Main Content */}
            <div className="lg:col-span-3 space-y-6">
              {publications.map((pub) => (
                  <PublicationCard key={pub.pubId} publication={pub} variant="default" />
              ))}
            </div>

            {/* Sidebar */}
            <HomeSidebar
                isLoggedIn={isLoggedIn}
                trendingTopics={trendingTopics}
                recentActivity={recentActivity}
                stats={isLoggedIn ? loggedInStats : guestStats}
            />
          </div>
        </div>
      </div>
  )
}
