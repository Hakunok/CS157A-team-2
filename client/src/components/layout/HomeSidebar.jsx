import {
  Card,
  CardHeader,
  CardTitle,
  CardContent
} from "@/components/ui/card"

import {
  Bookmark,
  BookOpen,
  Heart,
  Tally4
} from "lucide-react"

export function HomeSidebar({ isLoggedIn, recentActivity, stats }) {
  return (
    <div className="space-y-8">
      {/* Recent Activity */}
      <Card>
        <CardHeader className="pb-4 flex flex-row items-center gap-2">
          <BookOpen className="w-5 h-5 text-primary" />
          <CardTitle className="text-lg font-ui font-semibold">Recent Activity</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {recentActivity.map((activity, index) => (
            <div key={index} className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                {activity.type === "saved" && <Bookmark className="w-4 h-4 text-primary" />}
                {activity.type === "read" && <BookOpen className="w-4 h-4 text-primary" />}
                {activity.type === "liked" && <Heart className="w-4 h-4 text-primary" />}
              </div>
              <div className="flex-1 min-w-0">
                <p className="ui-text text-sm text-foreground truncate">
                  {activity.title}
                </p>
                <p className="ui-text text-xs text-muted-foreground">
                  {activity.time}
                </p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Quick Stats */}
      <Card>
        <CardHeader className="pb-4 flex flex-row items-center gap-2">
          <Tally4 className="w-5 h-5 text-primary" />
          <CardTitle className="text-lg font-ui font-semibold">
            {isLoggedIn ? "Your Library" : "Platform Stats"}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {isLoggedIn ? (
            <>
              <StatItem label="Publications Read" value={stats.read} />
              <StatItem label="Publications Liked" value={stats.liked} />
              <StatItem label="Publications Saved" value={stats.saved} />
            </>
          ) : (
            <>
              <StatItem label="Total Publications" value={stats.published} />
              <StatItem label="Times Read" value={stats.read} />
              <StatItem label="Total Likes" value={stats.liked} />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function StatItem({ label, value }) {
  return (
    <div className="flex items-center justify-between">
      <span className="ui-text text-sm text-muted-foreground">{label}</span>
      <span className="ui-text text-xs text-muted-foreground bg-muted px-2 py-1 rounded">
        {value}
      </span>
    </div>
  )
}