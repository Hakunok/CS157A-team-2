import { useTheme } from "next-themes"
import { Toaster as Sonner } from "sonner"

const Toaster = ({ ...props }) => {
  const { theme = "system" } = useTheme()

  return (
      <Sonner
          theme={theme}
          className="toaster group font-ui text-sm rounded-[var(--radius)]"
          style={{
            "--normal-bg": "var(--color-popover)",
            "--normal-text": "var(--color-popover-foreground)",
            "--normal-border": "var(--color-border)",
            "--success-bg": "color-mix(in oklch, var(--color-success) 90%, transparent)",
            "--error-bg": "color-mix(in oklch, var(--color-destructive) 85%, transparent)",
            "--info-bg": "color-mix(in oklch, var(--color-muted) 90%, transparent)",
            "--warning-bg": "color-mix(in oklch, var(--color-warning) 80%, transparent)",
          }}
          toastOptions={{
            classNames: {
              toast: "border border-[--normal-border] bg-[--normal-bg] text-[--normal-text] shadow-sm",
              title: "font-medium",
              description:
                  "text-[var(--color-muted-foreground)] text-sm font-content",
              actionButton:
                  "bg-[var(--color-primary)] text-[var(--color-primary-foreground)] font-ui text-sm px-3 py-1 rounded-[var(--radius)]",
              cancelButton:
                  "bg-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] font-ui text-sm px-3 py-1",
            },
          }}
          {...props}
      />
  )
}

export { Toaster }