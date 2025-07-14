import { useTheme } from "@/components/ui/theme-provider.jsx"
import { Button } from "@/components/ui/button.jsx"
import { Moon, Sun } from "lucide-react"

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()

  const nextTheme = theme === "dark" ? "light" : "dark"

  return (
      <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(nextTheme)}
          aria-label="Toggle theme"
      >
        <Sun
            className={`h-5 w-5 transition-all duration-300 ${
                theme === "dark" ? "rotate-0 scale-100" : "rotate-90 scale-0"
            }`}
        />
        <Moon
            className={`h-5 w-5 scale-x-[-1] absolute transition-all duration-300 ${
                theme === "dark" ? "rotate-90 scale-0" : "rotate-0 scale-100"
            }`}
        />
      </Button>
  )
}
