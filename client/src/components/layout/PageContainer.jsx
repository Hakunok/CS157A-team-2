import { cn } from "@/lib/utils"

export default function PageContainer({ children, className }) {
  return (
      <div className={cn("max-w-7xl mx-auto px-6", className)}>
        {children}
      </div>
  )
}
