import { useState } from "react"
import { Button } from "@/components/ui/button.jsx"
import { Checkbox } from "@/components/ui/checkbox.jsx"
import { Input } from "@/components/ui/input.jsx"
import { Label } from "@/components/ui/label.jsx"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog.jsx"
import { Card, CardContent } from "@/components/ui/card.jsx"
import { UserPen, Shield, ArrowRight, SkipForward, AlertCircle } from "lucide-react"

export default function StepTwo_Role({ formData, updateField, next, skip }) {
  // Local state to track role requests before submitting
  const [authorIntent, setAuthorIntent] = useState(formData?.requestAuthor || false)

  const [isAdminDialogOpen, setIsAdminDialogOpen] = useState(false)
  const [adminPassword, setAdminPassword] = useState("")
  const [isAdminVerified, setIsAdminVerified] = useState(formData?.isAdmin || false)
  const [isVerifying, setIsVerifying] = useState(false)
  const [adminError, setAdminError] = useState("")

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState("")

  const API_BASE_URL = "http://localhost:8080/airchive_war_exploded/api"

  const handleAdminPasswordSubmit = async () => {
    setIsVerifying(true)
    setAdminError("")
    try {
      const response = await fetch(`${API_BASE_URL}/roles/verify-admin`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: adminPassword }),
        credentials: "include",
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || "Invalid admin password")
      }

      // On success, mark as verified and close the dialog
      setIsAdminVerified(true)
      setIsAdminDialogOpen(false)
      setAdminPassword("")
    } catch (error) {
      console.error("Admin verification error:", error)
      setAdminError(error.message)
    } finally {
      setIsVerifying(false)
    }
  }

  const handleContinue = async () => {
    setIsSubmitting(true)
    setSubmitError("")

    const apiPromises = []

    // If author role was requested, create the fetch promise
    if (authorIntent) {
      const authorPromise = fetch(`${API_BASE_URL}/roles/request-author`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      }).then(res => {
        if (!res.ok) return res.json().then(err => { throw new Error(err.error || 'Author request failed') })
        return res.json()
      })
      apiPromises.push(authorPromise)
    }

    try {
      // Wait for all selected requests to complete
      await Promise.all(apiPromises)

      // If all were successful, update the parent form data and proceed
      updateField('requestAuthor', authorIntent)
      updateField('isAdmin', isAdminVerified)
      next()

    } catch (error) {
      console.error("Submission error:", error)
      setSubmitError(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleAdminCheck = (checked) => {
    if (checked) {
      // If user checks the box, show the dialog. Don't change the checked state yet.
      setIsAdminDialogOpen(true);
    } else {
      // If user unchecks the box, reset all admin states
      setIsAdminVerified(false);
    }
  }

  const handleDialogClose = (isOpen) => {
    setIsAdminDialogOpen(isOpen);
  }


  return (
      <div className="w-full space-y-8">
        <div className="text-center space-y-2">
          <h2 className="text-3xl font-bold tracking-tight">Select Your Roles</h2>
          <p className="text-muted-foreground text-lg">
            This step is optional. You will be able to change your roles later.
          </p>
        </div>

        <div className="space-y-8">
          {/* Author Request Card */}
          <Card className={`transition-all duration-200 ${
              authorIntent ? 'ring-2 ring-blue-500' : ''
          }`}>
            <CardContent className="p-6">
              <div className="grid grid-cols-[auto,1fr] items-start gap-6">
                <div className="bg-blue-100 text-blue-600 rounded-lg p-3 flex items-center justify-center">
                  <UserPen className="h-7 w-7" />
                </div>
                <div className="space-y-1">
                  <h3 className="text-lg font-semibold">Become an Author</h3>
                  <p className="text-muted-foreground">Request permission to write and publish articles on aiRchive.</p>
                  <div className="flex items-center space-x-2 pt-2">
                    <Checkbox
                        id="author-request"
                        checked={authorIntent}
                        onCheckedChange={setAuthorIntent}
                    />
                    <Label
                        htmlFor="author-request"
                        className="font-medium leading-none cursor-pointer"
                    >
                      I want to be an author!
                    </Label>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Admin Access Card */}
          <Card className={`transition-all duration-200 ${
              isAdminVerified ? 'ring-2 ring-red-500' : ''
          }`}>
            <CardContent className="p-6">
              <div className="grid grid-cols-[auto,1fr] items-start gap-6">
                <div className="bg-red-100 text-red-600 rounded-lg p-3 flex items-center justify-center">
                  <Shield className="h-7 w-7" />
                </div>
                <div className="space-y-1">
                  <h3 className="text-lg font-semibold">Administrator Access</h3>
                  <p className="text-muted-foreground">If you have credentials, verify your administrator status here.</p>
                  <div className="flex items-center space-x-2 pt-2">
                    <Checkbox
                        id="admin-access"
                        checked={isAdminVerified}
                        onCheckedChange={handleAdminCheck}
                    />
                    <Label
                        htmlFor="admin-access"
                        className="font-medium leading-none cursor-pointer"
                    >
                      I am an administrator
                    </Label>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Admin Password Dialog */}
        <Dialog open={isAdminDialogOpen} onOpenChange={handleDialogClose}>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5 text-red-600" />
                Admin Verification
              </DialogTitle>
              <DialogDescription>
                Enter the VERY secret admin password and you will be granted very special privileges.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-2 py-2">
              <Label htmlFor="admin-password">Admin Password</Label>
              <Input
                  id="admin-password"
                  type="password"
                  value={adminPassword}
                  onChange={(e) => setAdminPassword(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAdminPasswordSubmit()}
              />
              <p className="text-xs text-muted-foreground pt-1">
                Hint: The password is "1234".
              </p>
              {adminError && (
                  <p className="text-sm text-destructive flex items-center gap-2 pt-1">
                    <AlertCircle className="h-4 w-4" />
                    {adminError}
                  </p>
              )}
            </div>
            <DialogFooter className="flex gap-2">
              <Button
                  variant="outline"
                  onClick={() => setIsAdminDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button onClick={handleAdminPasswordSubmit} disabled={!adminPassword || isVerifying}>
                {isVerifying ? "Verifying..." : "Verify"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Action Buttons */}
        <div className="pt-6">
          <div className="flex justify-between items-center">
            <Button
                variant="ghost"
                onClick={skip}
                className="flex items-center gap-2 text-muted-foreground"
            >
              <SkipForward className="h-4 w-4" />
              Skip for now
            </Button>
            <Button onClick={handleContinue} size="lg" className="flex items-center gap-2" disabled={isSubmitting}>
              {isSubmitting ? "Submitting..." : "Continue"}
              <ArrowRight className="h-4 w-4" />
            </Button>
          </div>
          {submitError && (
              <p className="text-sm text-destructive text-right flex items-center justify-end gap-2 mt-2">
                <AlertCircle className="h-4 w-4" />
                {submitError}
              </p>
          )}
        </div>
      </div>
  )
}