import { Search, BookOpen, Star, TrendingUp, Clock, Filter, ArrowRight, Bookmark, Eye, MessageCircle } from 'lucide-react';

export default function HomePage() {
  const featuredArticles = [
    {
      id: 1,
      title: "Attention Is All You Need: Revisiting the Transformer Architecture",
      author: "Vaswani et al.",
      journal: "NIPS 2017",
      readTime: "12 min read",
      views: "2.4k",
      bookmarks: 156,
      tags: ["Transformers", "Attention", "NLP"],
      abstract: "We propose a new simple network architecture, the Transformer, based solely on attention mechanisms, dispensing with recurrence and convolutions entirely...",
      isBookmarked: true
    },
    {
      id: 2,
      title: "Large Language Models are Few-Shot Learners",
      author: "Brown et al.",
      journal: "NeurIPS 2020",
      readTime: "18 min read",
      views: "3.1k",
      bookmarks: 243,
      tags: ["GPT-3", "Few-shot Learning", "LLMs"],
      abstract: "Recent work has demonstrated substantial gains on many NLP tasks and benchmarks by pre-training on a large corpus of text followed by fine-tuning...",
      isBookmarked: false
    },
    {
      id: 3,
      title: "Constitutional AI: Harmlessness from AI Feedback",
      author: "Bai et al.",
      journal: "Anthropic 2022",
      readTime: "15 min read",
      views: "1.8k",
      bookmarks: 189,
      tags: ["AI Safety", "Constitutional AI", "RLHF"],
      abstract: "We propose Constitutional AI (CAI), a method for training a harmless AI assistant through self-improvement, without any human labels identifying harmful outputs...",
      isBookmarked: true
    }
  ];

  const trendingTopics = [
    { name: "Multimodal AI", count: 42 },
    { name: "AI Safety", count: 38 },
    { name: "Diffusion Models", count: 35 },
    { name: "Reinforcement Learning", count: 29 },
    { name: "Neural Architecture", count: 24 }
  ];

  const recentActivity = [
    { type: "bookmark", title: "Chain-of-Thought Prompting", time: "2 hours ago" },
    { type: "read", title: "Scaling Laws for Neural LMs", time: "4 hours ago" },
    { type: "comment", title: "DALL-E 2 Paper Discussion", time: "6 hours ago" }
  ];

  return (
      <div className="min-h-screen bg-background">

        <div className="max-w-7xl mx-auto px-6 py-8">
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Main Content */}
            <div className="lg:col-span-3 space-y-8">
              {/* Hero Section */}
              <section className="text-center py-12">
                <h2 className="text-4xl font-ui font-bold mb-4 text-balance">
                  Discover the Latest in AI Research
                </h2>
                <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto text-balance">
                  Curated collection of cutting-edge papers, insights, and discussions from the artificial intelligence research community.
                </p>
                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                  <button className="ui-text px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium">
                    Explore Papers
                  </button>
                  <button className="ui-text px-6 py-3 border border-border text-foreground rounded-lg hover:bg-surface transition-colors font-medium">
                    Browse by Topic
                  </button>
                </div>
              </section>

              {/* Featured Articles */}
              <section>
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-2xl font-ui font-bold">Featured Papers</h3>
                  <button className="ui-text text-sm text-primary hover:text-primary/80 transition-colors flex items-center gap-1">
                    View all <ArrowRight className="w-4 h-4" />
                  </button>
                </div>

                <div className="space-y-6">
                  {featuredArticles.map((article) => (
                      <article key={article.id} className="surface-elevated rounded-lg p-6 hover:shadow-lg transition-all duration-200">
                        <div className="flex items-start justify-between mb-4">
                          <div className="flex-1">
                            <h4 className="text-xl font-ui font-semibold mb-2 text-balance leading-tight">
                              {article.title}
                            </h4>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground ui-text mb-3">
                              <span>{article.author}</span>
                              <span>•</span>
                              <span>{article.journal}</span>
                              <span>•</span>
                              <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                                {article.readTime}
                          </span>
                            </div>
                          </div>
                          <button className={`p-2 rounded-lg transition-colors ${article.isBookmarked ? 'text-primary bg-primary/10' : 'text-muted-foreground hover:text-foreground'}`}>
                            <Bookmark className="w-5 h-5" fill={article.isBookmarked ? 'currentColor' : 'none'} />
                          </button>
                        </div>

                        <p className="content-text text-muted-foreground mb-4 leading-relaxed">
                          {article.abstract}
                        </p>

                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            {article.tags.map((tag) => (
                                <span key={tag} className="ui-text px-2 py-1 bg-secondary/10 text-secondary text-xs rounded-md">
                            {tag}
                          </span>
                            ))}
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground ui-text">
                        <span className="flex items-center gap-1">
                          <Eye className="w-4 h-4" />
                          {article.views}
                        </span>
                            <span className="flex items-center gap-1">
                          <Bookmark className="w-4 h-4" />
                              {article.bookmarks}
                        </span>
                          </div>
                        </div>
                      </article>
                  ))}
                </div>
              </section>
            </div>

            {/* Sidebar */}
            <div className="space-y-8">
              {/* Trending Topics */}
              <section className="surface-elevated rounded-lg p-6">
                <h3 className="text-lg font-ui font-semibold mb-4 flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-primary" />
                  Trending Topics
                </h3>
                <div className="space-y-3">
                  {trendingTopics.map((topic, index) => (
                      <div key={topic.name} className="flex items-center justify-between">
                        <button className="ui-text text-sm text-foreground hover:text-primary transition-colors text-left">
                          {topic.name}
                        </button>
                        <span className="ui-text text-xs text-muted-foreground bg-muted px-2 py-1 rounded">
                      {topic.count}
                    </span>
                      </div>
                  ))}
                </div>
              </section>

              {/* Recent Activity */}
              <section className="surface-elevated rounded-lg p-6">
                <h3 className="text-lg font-ui font-semibold mb-4 flex items-center gap-2">
                  <Clock className="w-5 h-5 text-primary" />
                  Recent Activity
                </h3>
                <div className="space-y-4">
                  {recentActivity.map((activity, index) => (
                      <div key={index} className="flex items-start gap-3">
                        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                          {activity.type === 'bookmark' && <Bookmark className="w-4 h-4 text-primary" />}
                          {activity.type === 'read' && <BookOpen className="w-4 h-4 text-primary" />}
                          {activity.type === 'comment' && <MessageCircle className="w-4 h-4 text-primary" />}
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
                </div>
              </section>

              {/* Quick Stats */}
              <section className="surface-elevated rounded-lg p-6">
                <h3 className="text-lg font-ui font-semibold mb-4">Your Library</h3>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="ui-text text-sm text-muted-foreground">Papers Read</span>
                    <span className="ui-text text-sm font-medium">127</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="ui-text text-sm text-muted-foreground">Bookmarked</span>
                    <span className="ui-text text-sm font-medium">43</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="ui-text text-sm text-muted-foreground">Reading Time</span>
                    <span className="ui-text text-sm font-medium">18.5h</span>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </div>
      </div>
  );
}