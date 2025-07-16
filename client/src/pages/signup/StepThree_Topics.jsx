import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button.jsx"
import { Checkbox } from "@/components/ui/checkbox.jsx"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog.jsx"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table.jsx"
import { AlertCircle, Check, Loader2, ListPlus } from "lucide-react"

const API_BASE_URL = "http://localhost:8080/server_war_exploded/api"

export default function StepThree_Topics({ onComplete }) {
  const [topics, setTopics] = useState([])
  const [selectedTopics, setSelectedTopics] = useState(new Set())
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  // Fetch topics from the backend when the dialog is first opened
  useEffect(() => {
    if (isDialogOpen && topics.length === 0) {
      const fetchTopics = async () => {
        setIsLoading(true)
        setError(null)
        try {
          const response = await fetch(`${API_BASE_URL}/topics`, {
            credentials: "include",
          })
          if (!response.ok) {
            throw new Error("Failed to fetch topics from the server.")
          }
          const data = await response.json()
          setTopics(data)
        } catch (err) {
          setError(err.message)
        } finally {
          setIsLoading(false)
        }
      }
      fetchTopics()
    }
  }, [isDialogOpen, topics.length])

  const handleSelectTopic = (topicCode) => {
    setSelectedTopics(prevSelected => {
      const newSelected = new Set(prevSelected)
      if (newSelected.has(topicCode)) {
        newSelected.delete(topicCode)
      } else {
        newSelected.add(topicCode)
      }
      return newSelected
    })
  }

  const handleSubmit = async () => {
    setIsSubmitting(true)
    setError(null)
    try {
      const response = await fetch(`${API_BASE_URL}/topics/interests`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ topicCodes: Array.from(selectedTopics) }),
        credentials: "include",
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || "Failed to save your interests.")
      }

      // If successful, call the onComplete prop to finish the signup flow
      onComplete()

    } catch (err) {
      setError(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
      <div className="w-full space-y-8">
        <div className="text-center space-y-2">
          <h2 className="text-3xl font-bold tracking-tight">Personalize Your Feed</h2>
          <p className="text-muted-foreground text-lg">
            Choose some topics you're interested in. This helps us recommend relevant content.
          </p>
        </div>

        <div className="flex flex-col items-center justify-center text-center p-8 border-2 border-dashed rounded-lg">
          <h3 className="text-lg font-semibold mb-2">Select Your Interests</h3>
          <p className="text-muted-foreground mb-4">
            You have selected {selectedTopics.size} topic(s).
          </p>
          <Button onClick={() => setIsDialogOpen(true)} size="lg">
            <ListPlus className="mr-2 h-4 w-4" />
            {selectedTopics.size > 0 ? "Edit Selections" : "Select Topics"}
          </Button>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogContent className="max-w-4xl h-[80vh] flex flex-col">
            <DialogHeader>
              <DialogTitle>Select Your Interests</DialogTitle>
              <DialogDescription>
                Click on a row to select or deselect a topic.
              </DialogDescription>
            </DialogHeader>

            <div className="flex-grow overflow-y-auto border rounded-lg">
              {isLoading && (
                  <div className="flex justify-center items-center h-full">
                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                  </div>
              )}

              {!isLoading && error && (
                  <div className="text-destructive flex flex-col items-center justify-center h-full gap-2">
                    <AlertCircle className="h-8 w-8" />
                    <p className="font-semibold">Could not load topics</p>
                    <p className="text-sm">{error}</p>
                  </div>
              )}

              {!isLoading && !error && (
                  <Table>
                    <TableHeader className="sticky top-0 bg-background">
                      <TableRow>
                        <TableHead className="w-[50px]"></TableHead>
                        <TableHead>Topic Code</TableHead>
                        <TableHead>Name</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {topics.map((topic) => (
                          <TableRow
                              key={topic.code}
                              onClick={() => handleSelectTopic(topic.code)}
                              className="cursor-pointer"
                              data-state={selectedTopics.has(topic.code) ? 'selected' : ''}
                          >
                            <TableCell>
                              <Checkbox
                                  checked={selectedTopics.has(topic.code)}
                                  onCheckedChange={() => handleSelectTopic(topic.code)}
                              />
                            </TableCell>
                            <TableCell className="text-sm">{topic.code}</TableCell>
                            <TableCell className="font-medium">
                              {topic.name}
                            </TableCell>
                          </TableRow>
                      ))}
                    </TableBody>
                  </Table>
              )}
            </div>

            <DialogFooter className="pt-4">
              {error && !isLoading && (
                  <p className="text-sm text-destructive flex items-center gap-2 mr-auto">
                    <AlertCircle className="h-4 w-4" />
                    {error}
                  </p>
              )}
              <Button variant="outline" onClick={() => setIsDialogOpen(false)}>Close</Button>
              <Button onClick={handleSubmit} className="flex items-center gap-2" disabled={isSubmitting || isLoading}>
                {isSubmitting ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      Completing Signup...
                    </>
                ) : (
                    <>
                      Complete Signup
                      <Check className="h-4 w-4" />
                    </>
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
  )
}