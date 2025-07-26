import { useTheme } from "next-themes"
import { Toaster as Sonner } from "sonner"

const Toaster = ({ ...props }) => {
  const { theme = "system" } = useTheme()

  return (
      <Sonner
          theme={theme}
          className="toaster group font-ui text-sm rounded-[var(--radius)]"
          style={{
            "--normal-bg": "var(--popover)",
            "--normal-text": "var(--popover-foreground)",
            "--normal-border": "var(--border)",
            "--success-bg": "color-mix(in oklch, var(--primary) 90%, transparent)",
            "--error-bg": "color-mix(in oklch, var(--destructive) 85%, transparent)",
            "--info-bg": "color-mix(in oklch, var(--muted) 90%, transparent)",
            "--warning-bg": "color-mix(in oklch, var(--primary) 70%, transparent)",
          }}
          toastOptions={{
            classNames: {
              toast: "border border-[--normal-border] bg-[--normal-bg] text-[--normal-text] shadow-sm",
              title: "font-medium",
              description: "text-muted-foreground text-sm font-content",
              actionButton: "bg-[--primary] text-[--primary-foreground] font-ui text-sm px-3 py-1 rounded-[var(--radius)]",
              cancelButton: "bg-transparent text-muted-foreground hover:text-foreground font-ui text-sm px-3 py-1",
            },
          }}
          {...props}
      />
  )
}

export { Toaster }
