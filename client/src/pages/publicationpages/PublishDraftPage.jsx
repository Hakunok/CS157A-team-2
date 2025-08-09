import React from "react"
import { useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"
import { Search, Plus, X, Calendar as CalendarIcon, Users, Tag, ArrowLeft } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle
} from "@/components/dialog.jsx"
import { Button } from "@/components/button.jsx"
import { Input } from "@/components/input.jsx"
import { Label } from "@/components/label.jsx"
import { Calendar } from "@/components/calendar.jsx"
import { Badge } from "@/components/badge.jsx"
import { Card, CardContent } from "@/components/card.jsx"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/popover.jsx"
import { KindBadge } from "@/components/KindBadge.jsx"
import { TopicBadge } from "@/components/TopicBadge.jsx"
import LoadingOverlay from "@/components/LoadingOverlay.jsx"
import { topicApi, publicationApi } from "@/lib/api"
import { cn } from "@/lib/utils"

export default function PublishDraftPage() {
  const { pubId } = useParams()
  const navigate = useNavigate()
  const [publication, setPublication] = React.useState(null)
  const [loading, setLoading] = React.useState(true)
  const [publishing, setPublishing] = React.useState(false)
  const [topics, setTopics] = React.useState([])
  const [selectedTopics, setSelectedTopics] = React.useState([])
  const [topicSearch, setTopicSearch] = React.useState("")
  const [filteredTopics, setFilteredTopics] = React.useState([])
  const [authorEmail, setAuthorEmail] = React.useState("")
  const [authors, setAuthors] = React.useState([])
  const [showCreateDialog, setShowCreateDialog] = React.useState(false)
  const [newAuthor, setNewAuthor] = React.useState({ email: "", firstName: "", lastName: "" })
  const [publishDate, setPublishDate] = React.useState(null)
  const [showCalendar, setShowCalendar] = React.useState(false)
  const [showContent, setShowContent] = React.useState(false)

  React.useEffect(() => {
    const loadData = async () => {
      try {
        const [pub, topicsData] = await Promise.all([
          publicationApi.getById(pubId),
          topicApi.getAll(),
        ]);
        setPublication(pub);
        setTopics(topicsData);
        setFilteredTopics(topicsData);
      } catch {
        toast.error("Failed to load publication data");
        navigate("/my-publications");
      } finally {
        setLoading(false);
        setTimeout(() => setShowContent(true), 100);
      }
    };

    loadData();
  }, [pubId, navigate]);


  React.useEffect(() => {
    if (!topicSearch.trim()) {
      setFilteredTopics(topics)
    } else {
      const filtered = topics.filter(topic =>
          topic.fullName.toLowerCase().includes(topicSearch.toLowerCase()) ||
          topic.code.toLowerCase().includes(topicSearch.toLowerCase())
      )
      setFilteredTopics(filtered)
    }
  }, [topicSearch, topics])

  const handleAddAuthor = async () => {
    const email = authorEmail.trim()
    if (!email) return

    try {
      const person = await publicationApi.getByEmail(email)
      if (authors.some((a) => a.personId === person.personId)) {
        toast.error("Author already added")
        return
      }
      setAuthors((prev) => [...prev, person])
      setAuthorEmail("")
      toast.success("Author added successfully")
    } catch {
      setNewAuthor((prev) => ({ ...prev, email }))
      setShowCreateDialog(true)
    }
  }

  const handleCreateAuthor = async () => {
    const { email, firstName, lastName } = newAuthor
    if (!email || !firstName || !lastName) {
      toast.error("Please fill in all fields")
      return
    }

    try {
      const authorData = {
        identityEmail: email,
        firstName,
        lastName
      }
      const person = await publicationApi.createAuthor(authorData)
      setAuthors((prev) => [...prev, person])
      setShowCreateDialog(false)
      setNewAuthor({ email: "", firstName: "", lastName: "" })
      toast.success("Author created and added successfully")
    } catch {
      toast.error("Failed to create author")
    }
  }

  const handleRemoveAuthor = (personId) => {
    setAuthors((prev) => prev.filter((a) => a.personId !== personId))
  }

  const handleToggleTopic = (topic) => {
    setSelectedTopics((prev) => {
      const isSelected = prev.some(t => t.topicId === topic.topicId)
      if (isSelected) {
        return prev.filter((t) => t.topicId !== topic.topicId)
      } else if (prev.length < 3) {
        return [...prev, topic]
      } else {
        toast.error("You can only select up to 3 topics")
        return prev
      }
    })
  }

  const handleRemoveTopic = (topicId) => {
    setSelectedTopics((prev) => prev.filter((t) => t.topicId !== topicId))
  }

  const handlePublish = async () => {
    if (!authors.length) {
      toast.error("Please add at least one author")
      return
    }

    if (!selectedTopics.length) {
      toast.error("Please select at least one topic")
      return
    }

    const data = {
      authorIds: authors.map((a) => a.personId),
      topicIds: selectedTopics.map((t) => t.topicId),
      publishedAt: publishDate?.toISOString() || undefined
    }

    try {
      setPublishing(true)
      await publicationApi.publish(pubId, data)
      toast.success("Publication published successfully!")
      navigate(`/publications/${pubId}`)
    } catch {
      toast.error("Failed to publish publication")
    } finally {
      setPublishing(false)
    }
  }

  if (loading) {
    return <LoadingOverlay message="Loading publication..." />
  }
  if (!publication) {
    return null
  }

  return (
      <div className="min-h-screen bg-background">
        {publishing && (
            <LoadingOverlay message="Publishing your work..." />
        )}

        {/* back to my publications */}
        <div className="max-w-6xl mx-auto px-6 pt-8">
          <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate("/my-publications")}
              className="gap-2 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to My Publications
          </Button>
        </div>

        <div
            className={cn(
                "max-w-5xl mx-auto px-6 pb-16 space-y-12 transition-opacity duration-500 ease-in-out",
                showContent ? "opacity-100" : "opacity-0 pointer-events-none"
            )}
        >
        {/* page header */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <h1 className="text-4xl font-semibold font-ui text-foreground">
                  Publish Draft
                </h1>
                <KindBadge kind={publication.kind} />
              </div>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-medium font-content text-foreground">
                {publication.title}
              </h2>
              <p className="text-muted-foreground font-ui">
                Complete the details below to publish your {publication.kind.toLowerCase()}
              </p>
            </div>
          </div>

          {/* choose authors section */}
          <Card>
            <CardContent className="p-6 space-y-4">
              <div className="flex items-center gap-2 mb-4">
                <Users className="w-5 h-5 text-muted-foreground" />
                <Label className="text-lg font-medium font-ui">Authors</Label>
              </div>

              <div className="space-y-4">
                <div className="flex gap-2">
                  <Input
                      value={authorEmail}
                      onChange={(e) => setAuthorEmail(e.target.value)}
                      placeholder="Enter author email address"
                      onKeyDown={(e) => e.key === 'Enter' && handleAddAuthor()}
                      className="flex-1"
                  />
                  <Button onClick={handleAddAuthor} className="gap-2">
                    <Plus className="w-4 h-4" />
                    Add Author
                  </Button>
                </div>

                {authors.length > 0 && (
                    <div className="space-y-2">
                      <p className="text-sm text-muted-foreground font-ui">
                        Selected authors ({authors.length}):
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {authors.map((author) => (
                            <Badge
                                key={author.personId}
                                className="bg-blue-500/10 text-blue-600 border border-blue-500/20 gap-1"
                            >
                              {author.firstName} {author.lastName}
                              <button
                                  onClick={() => handleRemoveAuthor(author.personId)}
                                  className="ml-1 hover:bg-blue-500/20 rounded-full p-0.5"
                              >
                                <X className="w-3 h-3" />
                              </button>
                            </Badge>
                        ))}
                      </div>
                    </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* choose topics section */}
          <Card>
            <CardContent className="p-6 space-y-4">
              <div className="flex items-center gap-2 mb-4">
                <Tag className="w-5 h-5 text-muted-foreground" />
                <Label className="text-lg font-medium font-ui">Topics</Label>
                <span className="text-sm text-muted-foreground">
                (Select up to 3)
              </span>
              </div>

              <div className="space-y-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                  <Input
                      value={topicSearch}
                      onChange={(e) => setTopicSearch(e.target.value)}
                      placeholder="Search topics..."
                      className="pl-10"
                  />
                </div>

                {selectedTopics.length > 0 && (
                    <div className="space-y-2">
                      <p className="text-sm text-muted-foreground font-ui">
                        Selected topics ({selectedTopics.length}/3):
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {selectedTopics.map((topic) => (
                            <TopicBadge
                                key={topic.topicId}
                                topic={topic}
                                variant="removable"
                                onRemove={handleRemoveTopic}
                            />
                        ))}
                      </div>
                    </div>
                )}

                <div className="max-h-60 overflow-y-auto border rounded-md p-2 space-y-1">
                  {filteredTopics.length === 0 ? (
                      <p className="text-sm text-muted-foreground text-center py-4">
                        No topics found
                      </p>
                  ) : (
                      filteredTopics.map((topic) => {
                        const isSelected = selectedTopics.some(t => t.topicId === topic.topicId)
                        return (
                            <button
                                key={topic.topicId}
                                onClick={() => handleToggleTopic(topic)}
                                disabled={!isSelected && selectedTopics.length >= 3}
                                className={cn(
                                    "w-full text-left p-2 rounded-md text-sm transition-colors",
                                    isSelected
                                        ? "bg-secondary text-secondary-foreground"
                                        : "hover:bg-muted",
                                    !isSelected && selectedTopics.length >= 3 && "opacity-50 cursor-not-allowed"
                                )}
                            >
                              <div className="font-medium">{topic.fullName}</div>
                              <div className="text-muted-foreground text-xs">{topic.code}</div>
                            </button>
                        )
                      })
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* choose publish date section */}
          <Card>
            <CardContent className="p-6 space-y-4">
              <div className="flex items-center gap-2 mb-4">
                <CalendarIcon className="w-5 h-5 text-muted-foreground" />
                <Label className="text-lg font-medium font-ui">Publish Date</Label>
                <span className="text-sm text-muted-foreground">
                (Optional)
              </span>
              </div>

              <div className="space-y-4">
                <Popover open={showCalendar} onOpenChange={setShowCalendar}>
                  <PopoverTrigger asChild>
                    <Button
                        variant="outline"
                        className={cn(
                            "w-full justify-start text-left font-normal",
                            !publishDate && "text-muted-foreground"
                        )}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {publishDate ? publishDate.toLocaleDateString() : "Select publish date"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                        mode="single"
                        selected={publishDate}
                        onSelect={(date) => {
                          setPublishDate(date)
                          setShowCalendar(false)
                        }}
                    />
                  </PopoverContent>
                </Popover>

                {publishDate && (
                    <div className="flex items-center gap-2">
                      <Badge variant="outline" className="gap-1">
                        {publishDate.toLocaleDateString()}
                        <button
                            onClick={() => setPublishDate(null)}
                            className="hover:bg-muted rounded-full p-0.5"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </Badge>
                    </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* save/cancel buttons */}
          <div className="flex items-center justify-between pt-6 border-t border-border">
            <Button
                variant="outline"
                onClick={() => navigate(`/my-publications/${pubId}/edit`)}
                className="gap-2"
            >
              Back to Edit
            </Button>

            <Button
                onClick={handlePublish}
                disabled={!authors.length || !selectedTopics.length || publishing}
                size="lg"
                className="gap-2"
            >
              Publish
            </Button>
          </div>
        </div>

        {/* dialog for creating a new author */}
        <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle className="font-ui text-lg">Create New Author</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 pt-2">
              <div className="space-y-2">
                <Label htmlFor="new-email" className="font-ui">Email</Label>
                <Input
                    id="new-email"
                    value={newAuthor.email}
                    onChange={(e) => setNewAuthor({ ...newAuthor, email: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="new-firstName" className="font-ui">First Name</Label>
                <Input
                    id="new-firstName"
                    value={newAuthor.firstName}
                    onChange={(e) => setNewAuthor({ ...newAuthor, firstName: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="new-lastName" className="font-ui">Last Name</Label>
                <Input
                    id="new-lastName"
                    value={newAuthor.lastName}
                    onChange={(e) => setNewAuthor({ ...newAuthor, lastName: e.target.value })}
                />
              </div>
              <div className="flex gap-2 pt-2">
                <Button
                    variant="outline"
                    onClick={() => setShowCreateDialog(false)}
                    className="flex-1"
                >
                  Cancel
                </Button>
                <Button
                    onClick={handleCreateAuthor}
                    className="flex-1 gap-2"
                >
                  <Plus className="w-4 h-4" />
                  Create Author
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
  )
}