import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/table.jsx";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/tabs.jsx";
import { Button } from "@/components/button.jsx";
import { Input } from "@/components/input.jsx";
import { Label } from "@/components/label.jsx";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/dialog.jsx";
import { Trash } from "lucide-react";
import { authorRequestApi, topicApi } from "@/lib/api.js";
import { toast } from "sonner";
import { cn } from "@/lib/utils.js";

export default function AdminDashboardPage() {
  const [tab, setTab] = React.useState("requests");
  const [requests, setRequests] = React.useState([]);
  const [topics, setTopics] = React.useState([]);
  const [page, setPage] = React.useState(1);
  const [hasMore, setHasMore] = React.useState(true);
  const [showCreateDialog, setShowCreateDialog] = React.useState(false);
  const [newCode, setNewCode] = React.useState("");
  const [newName, setNewName] = React.useState("");

  const [isInitialLoading, setIsInitialLoading] = React.useState(true);
  const [isRefreshing, setIsRefreshing] = React.useState(false);

  const pageSize = 10;

  const loadRequests = async (pageToLoad = 1) => {
    try {
      const newRequests = await authorRequestApi.getPending(pageToLoad, pageSize);
      if (pageToLoad === 1) {
        setRequests(newRequests);
      } else {
        setRequests((prev) => [...prev, ...newRequests]);
      }
      setHasMore(newRequests.length === pageSize);
    } catch (err) {
      toast.error("Failed to load requests");
    } finally {
      setIsInitialLoading(false);
      setIsRefreshing(false);
    }
  };

  const loadTopics = async () => {
    try {
      const res = await topicApi.getAll();
      setTopics(res);
    } catch {
      toast.error("Failed to load topics");
    } finally {
      setIsInitialLoading(false);
      setIsRefreshing(false);
    }
  };

  React.useEffect(() => {
    setIsInitialLoading(true);
    setIsRefreshing(true);

    const timeout = setTimeout(() => setIsRefreshing(false), 150);

    if (tab === "requests") {
      setPage(1);
      setHasMore(true);
      loadRequests(1);
    } else if (tab === "topics") {
      loadTopics();
    }

    return () => clearTimeout(timeout);
  }, [tab]);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadRequests(nextPage);
  };

  const handleApprove = async (accountId) => {
    try {
      await authorRequestApi.approve(accountId);
      setRequests((prev) => prev.filter((r) => r.accountId !== accountId));
      toast.success("Author request approved");
    } catch {
      toast.error("Failed to approve request");
    }
  };

  const handleCreateTopic = async () => {
    if (!newCode.trim() || !newName.trim()) {
      toast.error("Please fill out both fields");
      return;
    }
    try {
      const newTopic = await topicApi.create({
        code: newCode.trim(),
        fullName: newName.trim(),
      });
      setTopics((prev) => [...prev, newTopic]);
      toast.success("Topic created");
      setNewCode("");
      setNewName("");
      setShowCreateDialog(false);
    } catch {
      toast.error("Failed to create topic");
    }
  };

  const handleDeleteTopic = async (id) => {
    try {
      await topicApi.delete(id);
      setTopics((prev) => prev.filter((t) => t.topicId !== id));
      toast.success("Topic deleted");
    } catch {
      toast.error("Failed to delete topic");
    }
  };

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
                <Button onClick={() => setShowCreateDialog(true)}>+ Create Topic</Button>
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
              <div
                  className={cn(
                      "transition-opacity duration-500 ease-in-out",
                      isInitialLoading
                          ? "opacity-0"
                          : isRefreshing
                              ? "opacity-50 pointer-events-none"
                              : "opacity-100"
                  )}
              >
                {requests.length === 0 ? (
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
                                  {new Date(
                                      r.requestedAt[0],
                                      r.requestedAt[1] - 1,
                                      r.requestedAt[2],
                                      r.requestedAt[3] + 7,
                                      r.requestedAt[4],
                                      r.requestedAt[5]
                                  ).toLocaleString(undefined, {
                                    dateStyle: "medium",
                                    timeStyle: "short",
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
              </div>
            </TabsContent>

            <TabsContent value="topics">
              <div
                  className={cn(
                      "transition-opacity duration-500 ease-in-out",
                      isInitialLoading
                          ? "opacity-0"
                          : isRefreshing
                              ? "opacity-50 pointer-events-none"
                              : "opacity-100"
                  )}
              >
                {topics.length === 0 ? (
                    <p className="text-muted-foreground font-content">No topics found.</p>
                ) : (
                    <div className="space-y-6">
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
                    </div>
                )}
              </div>
            </TabsContent>
          </Tabs>

          {/* dialog for creatting a new topic */}
          <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
            <DialogContent className="sm:max-w-sm">
              <DialogHeader>
                <DialogTitle className="font-ui text-base">Create New Topic</DialogTitle>
              </DialogHeader>
              <div className="space-y-4 pt-2">
                <div className="space-y-1">
                  <Label htmlFor="code" className="text-sm font-ui">
                    Topic Code
                  </Label>
                  <Input
                      id="code"
                      placeholder="e.g. NLP"
                      value={newCode}
                      onChange={(e) => setNewCode(e.target.value)}
                  />
                </div>
                <div className="space-y-1">
                  <Label htmlFor="name" className="text-sm font-ui">
                    Full Name
                  </Label>
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
  );
}