// src/pages/SignupPage.jsx
import MultiStepSignup from "@/components/signup/MultiStepSignup"
import { GalleryVerticalEnd } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useNavigate } from "react-router-dom"

export default function SignupPage() {
  const navigate = useNavigate()

  return (
      <div className="bg-background flex h-[calc(100svh-4rem)] flex-col items-center justify-start gap-4 px-4 md:px-6 pt-12 overflow-y-auto">
      <div className="w-full max-w-md space-y-6">
          <div className="text-center space-y-2">
            <div className="flex justify-center items-center gap-2">
              <GalleryVerticalEnd className="size-6" />
              <span className="sr-only">aiRchive</span>
            </div>
            <h1 className="text-xl font-bold">Join aiRchive</h1>
          </div>

          <MultiStepSignup
              onComplete={() => {
                navigate("/dashboard")
              }}
          />
        </div>
      </div>
  )
}
