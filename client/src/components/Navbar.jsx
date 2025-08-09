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
} from "@/components/navigation-menu.jsx"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from "@/components/dropdown-menu.jsx"
import { Button } from "@/components/button.jsx"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/avatar.jsx"
import { useAuth } from "@/context/AuthContext.jsx"
import { publicationApi } from "@/lib/api.js"
import debounce from "lodash.debounce"
import {SearchCommand} from "@/components/SearchCommand.jsx";

export default function Navbar() {
  const { user, logout, isAuthor, isReader, isAdmin, loading: isAuthLoading } = useAuth()
  const navigate = useNavigate()
  const isLoggedIn = !!user

  const [openSearch, setOpenSearch] = React.useState(false)
  const [searchQuery, setSearchQuery] = React.useState("")
  const [results, setResults] = React.useState([])
  const [isLoading, setIsLoading] = React.useState(false)

  const performSearch = React.useCallback(
      debounce((query) => {
        if (query.trim().length >= 2) {
          setIsLoading(true)
          publicationApi
          .search(query.trim())
          .then((res) => setResults(res))
          .catch(() => setResults([]))
          .finally(() => setIsLoading(false))
        } else {
          setResults([])
        }
      }, 300),
      []
  )

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

  React.useEffect(() => {
    performSearch(searchQuery)
  }, [searchQuery, performSearch])

  if (isAuthLoading) {
    return (
        <nav className="sticky top-0 z-50 h-16 bg-[var(--color-background)] border-b border-[var(--color-border)]/40" />
    );
  }

  return (
      <>
        <nav className="sticky top-0 z-50 flex h-16 items-center justify-between
        px-6 border-b border-[var(--color-border)]/40 bg-[var(--color-background)]
        text-[var(--color-foreground)] font-ui text-sm backdrop-blur-sm">
          {/* Logo */}
          <div className="flex items-center">
            <Link
                to="/"
                className="text-lg font-semibold tracking-tight text-[var(--color-foreground)]
                hover:text-[var(--color-primary)] transition-colors duration-200"
            >
              aiRchive
            </Link>
          </div>

          {/* Search Bar */}
          <div className="flex-1 max-w-md mx-8">
            <SearchCommand />
          </div>


          {/* Navigation + User Menu */}
          <div className="flex items-center gap-4">
            <NavigationMenu>
              <NavigationMenuList>
                <NavigationMenuItem>
                  <NavigationMenuTrigger
                      onClick={() => navigate('/publications')}
                      className="h-9 px-3 text-sm font-medium text-[var(--color-foreground)]
                      hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]
                      data-[state=open]:bg-[var(--color-muted)]/80
                      data-[state=open]:text-[var(--color-foreground)] transition-all duration-200 cursor-pointer"
                  >
                    Publications
                  </NavigationMenuTrigger>
                  <NavigationMenuContent>
                    <div className="flex flex-col p-2 min-w-[180px] bg-[var(--color-popover)]/95 backdrop-blur-sm">
                      <NavigationMenuLink asChild>
                        <Link to="/papers" className="nav-subitem">Papers</Link>
                      </NavigationMenuLink>
                      <NavigationMenuLink asChild>
                        <Link to="/blogs" className="nav-subitem">Blogs & Articles</Link>
                      </NavigationMenuLink>
                    </div>
                  </NavigationMenuContent>
                </NavigationMenuItem>
                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link to="/collections" className="nav-item">Collections</Link>
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
                    <button className="rounded-full transition-all duration-200 focus:outline-none">
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
                      <Link to="/profile" className="cursor-pointer">My Profile</Link>
                    </DropdownMenuItem>
                    {isAuthor && (
                        <DropdownMenuItem asChild>
                          <Link to="/my-publications" className="cursor-pointer">My Publications</Link>
                        </DropdownMenuItem>
                    )}
                    {(isReader || isAuthor) && (
                        <DropdownMenuItem asChild>
                          <Link to="/my-collections" className="cursor-pointer">My Collections</Link>
                        </DropdownMenuItem>
                    )}
                    {isAdmin && (
                        <DropdownMenuItem asChild>
                          <Link to="/admin" className="cursor-pointer">My Administration</Link>
                        </DropdownMenuItem>
                    )}
                    <DropdownMenuItem
                        onSelect={logout}
                        className="text-[var(--color-destructive)] hover:bg-[var(--color-destructive)]/10
                        hover:text-[var(--color-destructive)] cursor-pointer"
                    >
                      Sign Out
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
            )}
          </div>
        </nav>
      </>
  )
}