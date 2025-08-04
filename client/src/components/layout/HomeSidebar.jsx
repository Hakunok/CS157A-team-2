import {
  Card,
  CardHeader,
  CardTitle,
  CardContent
} from "@/components/ui/card";

import {
  Bookmark,
  BookOpen,
  Heart,
  BarChart3,
  Eye,
  Star,
  TrendingUp
} from "lucide-react";

export function HomeSidebar({
  isLoggedIn,
  userTopics,
  userInteractions,
  stats,
  className,
  ...props
}) {
  return (
      <div className={`space-y-8 ${className || ''}`} {...props}>
        {isLoggedIn ? (
            <>
              {userTopics && userTopics.length > 0 && (
                  <Card>
                    <CardHeader className="pb-4 flex flex-row items-center gap-2">
                      <Star className="w-5 h-5 text-primary" />
                      <CardTitle className="text-lg font-ui font-semibold">Topics You Enjoy</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {userTopics.map((topic, index) => (
                          <div key={topic.name} className="flex items-center justify-between">
                            <button className="ui-text text-sm text-foreground hover:text-primary transition-colors text-left">
                              {topic.name}
                            </button>
                            <div className="flex items-center gap-1">
                              <div className="w-12 h-1.5 bg-muted rounded-full overflow-hidden">
                                <div
                                    className="h-full bg-primary rounded-full transition-all duration-300"
                                    style={{ width: `${topic.affinity}%` }}
                                />
                              </div>
                              <span className="ui-text text-xs text-muted-foreground w-8 text-right">
                        {topic.affinity}%
                      </span>
                            </div>
                          </div>
                      ))}
                    </CardContent>
                  </Card>
              )}

              {/* Recent Interactions */}
              {userInteractions && userInteractions.length > 0 && (
                  <Card>
                    <CardHeader className="pb-4 flex flex-row items-center gap-2">
                      <TrendingUp className="w-5 h-5 text-primary" />
                      <CardTitle className="text-lg font-ui font-semibold">Recent Activity</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      {userInteractions.map((interaction, index) => (
                          <InteractionItem key={index} interaction={interaction} />
                      ))}
                    </CardContent>
                  </Card>
              )}

              {/* User Stats */}
              <Card>
                <CardHeader className="pb-4 flex flex-row items-center gap-2">
                  <BarChart3 className="w-5 h-5 text-primary" />
                  <CardTitle className="text-lg font-ui font-semibold">Your Library</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <StatItem label="Publications Read" value={stats.read} />
                  <StatItem label="Publications Liked" value={stats.liked} />
                  <StatItem label="Publications Saved" value={stats.saved} />
                </CardContent>
              </Card>
            </>
        ) : (
            /* Platform Stats for Non-logged in users */
            <Card>
              <CardHeader className="pb-4 flex flex-row items-center gap-2">
                <BarChart3 className="w-5 h-5 text-primary" />
                <CardTitle className="text-lg font-ui font-semibold">Platform Stats</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <StatItem label="Total Publications" value={stats.published} />
                <StatItem label="Total Views" value={stats.views} />
                <StatItem label="Total Likes" value={stats.likes} />
              </CardContent>
            </Card>
        )}
      </div>
  );
}

function InteractionItem({ interaction }) {
  const getInteractionIcon = (type) => {
    switch (type) {
      case 'saved':
        return <Bookmark className="w-4 h-4 text-primary" />;
      case 'liked':
        return <Heart className="w-4 h-4 text-primary" />;
      case 'viewed':
        return <Eye className="w-4 h-4 text-primary" />;
      default:
        return <BookOpen className="w-4 h-4 text-primary" />;
    }
  };

  const getInteractionText = (type) => {
    switch (type) {
      case 'saved':
        return 'Saved';
      case 'liked':
        return 'Liked';
      case 'viewed':
        return 'Viewed';
      default:
        return 'Interacted with';
    }
  };

  return (
      <div className="flex items-start gap-3">
        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
          {getInteractionIcon(interaction.type)}
        </div>
        <div className="flex-1 min-w-0">
          <p className="ui-text text-sm text-foreground">
            <span className="text-muted-foreground">{getInteractionText(interaction.type)}</span>{' '}
            <span className="truncate font-medium">{interaction.publicationTitle}</span>
          </p>
          <p className="ui-text text-xs text-muted-foreground">
            {interaction.time}
          </p>
        </div>
      </div>
  );
}

function StatItem({ label, value }) {
  const formatValue = (val) => {
    if (typeof val === 'number') {
      if (val >= 1000000) {
        return (val / 1000000).toFixed(1) + 'M';
      } else if (val >= 1000) {
        return (val / 1000).toFixed(1) + 'K';
      }
      return val.toLocaleString();
    }
    return val;
  };

  return (
      <div className="flex items-center justify-between">
        <span className="ui-text text-sm text-muted-foreground">{label}</span>
        <span className="ui-text text-sm font-medium">
        {formatValue(value)}
      </span>
      </div>
  );
}