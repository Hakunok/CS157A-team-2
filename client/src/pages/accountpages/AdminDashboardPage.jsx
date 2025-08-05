import React from "react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from "@/components/ui/table.jsx"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs.jsx"
import { Button } from "@/components/ui/button.jsx"
import { Input } from "@/components/ui/input.jsx"
import { Label } from "@/components/ui/label.jsx"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog.jsx"
import { Trash } from "lucide-react"
import { authorRequestApi, topicApi } from "@/lib/api.js"
import LoadingOverlay from "@/components/shared/LoadingOverlay.jsx"
import { toast } from "sonner"

export default function AdminDashboardPage() {
  const [tab, setTab] = React.useState("requests")
  const [loading, setLoading] = React.useState(true)
  const [requests, setRequests] = React.useState([])
  const [page, setPage] = React.useState(1)
  const [hasMore, setHasMore] = React.useState(true)
  const [topics, setTopics] = React.useState([])
  const [showCreateDialog, setShowCreateDialog] = React.useState(false)
  const [newCode, setNewCode] = React.useState("")
  const [newName, setNewName] = React.useState("")
  const pageSize = 10

  const loadRequests = async (pageToLoad = 1) => {
    try {
      const newRequests = await authorRequestApi.getPending(pageToLoad, pageSize)
      if (pageToLoad === 1) {
        setRequests(newRequests)
      } else {
        setRequests((prev) => [...prev, ...newRequests])
      }
      setHasMore(newRequests.length === pageSize)
    } catch (err) {
      console.error("Failed to load requests", err)
      toast.error("Failed to load requests")
    } finally {
      setLoading(false)
    }
  }

  const loadTopics = async () => {
    try {
      const res = await topicApi.getAll()
      setTopics(res)
    } catch {
      toast.error("Failed to load topics")
    }
  }

  React.useEffect(() => {
    if (tab === "requests") {
      setPage(1)
      setHasMore(true)
      setLoading(true)
      loadRequests(1)
    } else if (tab === "topics") {
      setLoading(true)
      loadTopics().finally(() => setLoading(false))
    }
  }, [tab])

  const handleLoadMore = () => {
    const nextPage = page + 1
    setPage(nextPage)
    loadRequests(nextPage)
  }

  const handleApprove = async (accountId) => {
    try {
      await authorRequestApi.approve(accountId)
      setRequests((prev) => prev.filter((r) => r.accountId !== accountId))
      toast.success("Author request approved")
    } catch (err) {
      toast.error("Failed to approve request")
    }
  }

  const handleCreateTopic = async () => {
    if (!newCode.trim() || !newName.trim()) {
      toast.error("Please fill out both fields")
      return
    }
    try {
      const newTopic = await topicApi.create({ code: newCode.trim(), fullName: newName.trim() })
      setTopics((prev) => [...prev, newTopic])
      toast.success("Topic created")
      setNewCode("")
      setNewName("")
      setShowCreateDialog(false)
    } catch {
      //
    }
  }

  const handleDeleteTopic = async (id) => {
    try {
      await topicApi.delete(id)
      setTopics((prev) => prev.filter((t) => t.topicId !== id))
      toast.success("Topic deleted")
    } catch {
      toast.error("Failed to delete topic")
    }
  }

  return (
      <section className="min-h-screen bg-background py-16 px-6">
        <div className="max-w-5xl mx-auto space-y-12">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-semibold font-ui text-foreground mb-2">
                Administration
              </h1>
              <p className="text-sm text-muted-foreground font-ui">
                Manage author requests and topics
              </p>
            </div>
            {tab === "topics" && (
                <Button onClick={() => setShowCreateDialog(true)}>
                  + Create Topic
                </Button>
            )}
          </div>

          <Tabs value={tab} onValueChange={setTab}>
            <TabsList className="mb-6">
              <TabsTrigger value="requests" className="font-ui">
                Author Requests
              </TabsTrigger>
              <TabsTrigger value="topics" className="font-ui">
                Topic Management
              </TabsTrigger>
            </TabsList>

            <TabsContent value="requests">
              {loading ? (
                  <LoadingOverlay message="Loading author requests..." />
              ) : requests.length === 0 ? (
                  <p className="text-muted-foreground font-content">No pending requests.</p>
              ) : (
                  <>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Email</TableHead>
                          <TableHead>Requested At</TableHead>
                          <TableHead className="text-right"></TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {requests.map((r) => (
                            <TableRow key={r.accountId}>
                              <TableCell>{r.email}</TableCell>
                              <TableCell>
                                {new Date(...r.requestedAt).toLocaleString(undefined, {
                                  dateStyle: "medium",
                                  timeStyle: "short"
                                })}
                              </TableCell>
                              <TableCell className="text-right">
                                <Button
                                    size="sm"
                                    variant="default"
                                    onClick={() => handleApprove(r.accountId)}
                                >
                                  Approve
                                </Button>
                              </TableCell>
                            </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                    {hasMore && (
                        <div className="pt-6 text-center">
                          <Button variant="ghost" onClick={handleLoadMore}>
                            Load More
                          </Button>
                        </div>
                    )}
                  </>
              )}
            </TabsContent>

            <TabsContent value="topics">
              {loading ? (
                  <LoadingOverlay message="Loading topics..." />
              ) : (
                  <div className="space-y-6">
                    {topics.length === 0 ? (
                        <p className="text-muted-foreground font-content">No topics found.</p>
                    ) : (
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>Code</TableHead>
                              <TableHead>Name</TableHead>
                              <TableHead className="text-right"></TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {topics.map((t) => (
                                <TableRow key={t.topicId}>
                                  <TableCell>{t.code}</TableCell>
                                  <TableCell>{t.fullName}</TableCell>
                                  <TableCell className="text-right">
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="text-destructive hover:bg-destructive/10"
                                        onClick={() => handleDeleteTopic(t.topicId)}
                                    >
                                      <Trash className="h-4 w-4" />
                                    </Button>
                                  </TableCell>
                                </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                    )}
                  </div>
              )}
            </TabsContent>
          </Tabs>

          {/* Create Topic Dialog */}
          <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
            <DialogContent className="sm:max-w-sm">
              <DialogHeader>
                <DialogTitle className="font-ui text-base">Create New Topic</DialogTitle>
              </DialogHeader>
              <div className="space-y-4 pt-2">
                <div className="space-y-1">
                  <Label htmlFor="code" className="text-sm font-ui">Topic Code</Label>
                  <Input
                      id="code"
                      placeholder="e.g. NLP"
                      value={newCode}
                      onChange={(e) => setNewCode(e.target.value)}
                  />
                </div>
                <div className="space-y-1">
                  <Label htmlFor="name" className="text-sm font-ui">Full Name</Label>
                  <Input
                      id="name"
                      placeholder="e.g. Natural Language Processing"
                      value={newName}
                      onChange={(e) => setNewName(e.target.value)}
                  />
                </div>
                <Button className="w-full" onClick={handleCreateTopic}>
                  Create Topic
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </section>
  )
}