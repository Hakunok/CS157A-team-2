import Spinner from "@/components/ui/spinner"

export default function LoadingOverlay({ message = "Loading..." }) {
  return (
      <div className="fixed inset-0 z-50 backdrop-blur-sm bg-black/30 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3 p-6 rounded-md shadow-lg bg-[--color-card] border border-[--color-border]">
          <Spinner size="md" />
          <span className="text-sm text-muted-foreground">{message}</span>
        </div>
      </div>
  )
}
