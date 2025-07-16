import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ThemeToggle } from "@/components/ThemeToggle.jsx"

export default function Navbar({ isAuthenticated, onLogout }) {
  return (
      <header className="sticky top-0 z-50 h-16 px-4 flex justify-between
      items-center border-b shadow-sm bg-white dark:bg-black">
        {/* Left side (logo or nav) */}
        <div className="flex items-center gap-2">
          <Link to="/" className="text-xl font-bold">
            aiRchive
          </Link>
        </div>

        {/* Right side (buttons, toggles, etc.) */}
        <nav className="flex items-center gap-2">
          <ThemeToggle />
          {isAuthenticated ? (
              <>
                <Link to="/dashboard">
                  <Button variant="outline">Dashboard</Button>
                </Link>
                <Button onClick={onLogout} variant="destructive">Logout</Button>
              </>
          ) : (
              <>
                <Link to="/signin">
                  <Button variant="outline">Sign In</Button>
                </Link>
                <Link to="/signup">
                  <Button>Sign Up</Button>
                </Link>
              </>
          )}
        </nav>
      </header>
  )
}