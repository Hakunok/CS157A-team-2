import { useState, useEffect } from 'react'
import { cn } from "@/lib/utils"
import Spinner from "@/components/ui/spinner.jsx"

export default function LoadingOverlay({
  isActive,
  message = "Loading..."
}) {
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    if (isActive) {
      setIsVisible(true)
    } else if (!isActive && isVisible) {
      // When isActive becomes false, wait for animation to finish before hiding
      const timer = setTimeout(() => setIsVisible(false), 300) // Duration should match animation
      return () => clearTimeout(timer)
    }
  }, [isActive, isVisible])

  // Don't render the component if it's not supposed to be visible
  if (!isVisible) {
    return null
  }

  return (
      <div
          className={cn(
              "fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm",
              // Use animate-in/out for graceful transitions
              "animate-in fade-in-0 duration-300",
              !isActive && "animate-out fade-out-0 duration-300"
          )}
      >
        <div className="flex flex-col items-center gap-3 rounded-lg border bg-card p-6 text-card-foreground shadow-lg">
          <Spinner size="lg" />
          <span className="text-sm text-muted-foreground">{message}</span>
        </div>
      </div>
  )
}