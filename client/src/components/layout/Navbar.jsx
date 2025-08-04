"use client"

import * as React from "react"
import { Link, useNavigate } from "react-router-dom"
import {
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuTrigger,
  NavigationMenuContent,
  NavigationMenuLink
} from "@/components/ui/navigation-menu.jsx"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from "@/components/ui/dropdown-menu.jsx"
import { Button } from "@/components/ui/button.jsx"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar.jsx"
import { CommandDialog, CommandInput } from "@/components/ui/command"
import { useAuth } from "@/context/AuthContext"

export default function Navbar() {
  const { user, logout, isAuthor, isReader, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [openSearch, setOpenSearch] = React.useState(false)
  const isLoggedIn = !!user

  React.useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        setOpenSearch(true)
      }
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [])

  return (
      <>
        <nav className="sticky top-0 z-50 flex h-16 items-center justify-between px-6 border-b border-[var(--color-border)]/40 bg-[var(--color-background)] text-[var(--color-foreground)] font-ui text-sm backdrop-blur-sm">
          {/* Left: Logo */}
          <div className="flex items-center">
            <Link
                to="/"
                className="text-lg font-semibold tracking-tight text-[var(--color-foreground)] hover:text-[var(--color-primary)] transition-colors duration-200"
            >
              aiRchive
            </Link>
          </div>

          {/* Center: Search Bar */}
          <div className="flex-1 max-w-md mx-8">
            <button
                onClick={() => setOpenSearch(true)}
                className="w-full h-9 px-3 py-2 text-left text-sm text-[var(--color-muted-foreground)] bg-[var(--color-muted)]/30 border border-[var(--color-border)]/50 rounded-[var(--radius)] hover:bg-[var(--color-muted)]/50 hover:border-[var(--color-border)] transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-background focus-visible:ring-offset-2"
            >
              <div className="flex items-center gap-2">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth={2}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                >
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <span>Search publications...</span>
                <div className="ml-auto">
                  <kbd className="pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded-[var(--radius-sm)] border bg-[var(--color-muted)] px-1.5 font-mono text-[10px] font-medium text-[var(--color-muted-foreground)] opacity-100">
                    <span className="text-xs">⌘</span>K
                  </kbd>
                </div>
              </div>
            </button>
          </div>

          {/* Right: Navigation + Auth Menu */}
          <div className="flex items-center gap-4">
            {/* Publications + Collections */}
            <NavigationMenu>
              <NavigationMenuList>
                <NavigationMenuItem>
                  <NavigationMenuTrigger className="h-9 px-3 text-sm font-medium text-[var(--color-foreground)] hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)] data-[state=open]:bg-[var(--color-muted)]/80 data-[state=open]:text-[var(--color-foreground)] transition-all duration-200">
                    Publications
                  </NavigationMenuTrigger>
                  <NavigationMenuContent>
                    <div className="flex flex-col p-2 min-w-[180px] bg-[var(--color-popover)]/95 backdrop-blur-sm">
                      <NavigationMenuLink asChild>
                        <Link to="/papers" className="nav-subitem">
                          Papers
                        </Link>
                      </NavigationMenuLink>
                      <NavigationMenuLink asChild>
                        <Link to="/articles" className="nav-subitem">
                          Articles
                        </Link>
                      </NavigationMenuLink>
                      <NavigationMenuLink asChild>
                        <Link to="/blogs" className="nav-subitem">
                          Blogs
                        </Link>
                      </NavigationMenuLink>
                    </div>
                  </NavigationMenuContent>
                </NavigationMenuItem>

                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link to="/collections" className="nav-item">
                      Collections
                    </Link>
                  </NavigationMenuLink>
                </NavigationMenuItem>
              </NavigationMenuList>
            </NavigationMenu>

            {!isLoggedIn ? (
                <div className="flex gap-3">
                  <Button asChild variant="outline" size="sm" className="h-9 px-4">
                    <Link to="/signin">Sign In</Link>
                  </Button>
                  <Button asChild size="sm" className="h-9 px-4">
                    <Link to="/signup">Sign Up</Link>
                  </Button>
                </div>
            ) : (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button className="rounded-full hover:ring-2 hover:ring-[var(--color-ring)]/20 transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-background focus-visible:ring-offset-2">
                      <Avatar className="hover:scale-105 transition-transform duration-200">
                        <AvatarImage src="" alt="" />
                        <AvatarFallback
                            firstName={user.firstName}
                            lastName={user.lastName}
                            className="text-sm"
                        />
                      </Avatar>
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-48">
                    <DropdownMenuItem asChild>
                      <Link to="/profile" className="cursor-pointer">
                        My Profile
                      </Link>
                    </DropdownMenuItem>

                    {isAuthor && (
                        <DropdownMenuItem asChild>
                          <Link to="/my-publications" className="cursor-pointer">
                            My Publications
                          </Link>
                        </DropdownMenuItem>
                    )}

                    {(isReader || isAuthor) && (
                        <DropdownMenuItem asChild>
                          <Link to="/my-collections" className="cursor-pointer">
                            My Collections
                          </Link>
                        </DropdownMenuItem>
                    )}

                    {isAdmin && (
                        <DropdownMenuItem asChild>
                          <Link to="/admin" className="cursor-pointer">
                            My Administration
                          </Link>
                        </DropdownMenuItem>
                    )}

                    <DropdownMenuItem
                        onSelect={logout}
                        className="text-[var(--color-destructive)] hover:bg-[var(--color-destructive)]/10 hover:text-[var(--color-destructive)] cursor-pointer"
                    >
                      Sign Out
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
            )}
          </div>
        </nav>

        {/* Command Palette */}
        <CommandDialog open={openSearch} onOpenChange={setOpenSearch}>
          <CommandInput placeholder="Search publications by title" />
        </CommandDialog>
      </>
  )
}