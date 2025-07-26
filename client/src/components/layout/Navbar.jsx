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
import { Avatar, AvatarFallback } from "@/components/ui/avatar.jsx"
import { useAuth } from "@/hooks/useAuth"

export default function Navbar() {
  const { user, role, signOut } = useAuth()
  const navigate = useNavigate()
  const isLoggedIn = !!user

  return (
      <nav className="flex h-16 items-center justify-between px-6 border-b border-border/40 bg-background text-foreground font-ui text-sm backdrop-blur-sm">
        {/* Left: Brand only */}
        <div className="flex items-center">
          <Link
              to="/"
              className="text-lg font-semibold tracking-tight text-foreground hover:text-primary transition-colors duration-200"
          >
            aiRchive
          </Link>
        </div>

        {/* Right: Navigation + Auth/User Menu */}
        <div className="flex items-center gap-4">
          <NavigationMenu>
            <NavigationMenuList>
              <NavigationMenuItem>
                <NavigationMenuTrigger className="h-9 px-3 text-sm font-medium text-foreground hover:bg-muted/60 hover:text-foreground data-[state=open]:bg-muted/80 data-[state=open]:text-foreground transition-all duration-200">
                  Publications
                </NavigationMenuTrigger>
                <NavigationMenuContent>
                  <div className="flex flex-col p-2 min-w-[180px] bg-popover/95 backdrop-blur-sm">
                    <NavigationMenuLink asChild>
                      <Link
                          to="/publications/papers"
                          className="flex items-center h-9 px-3 text-sm font-medium text-popover-foreground hover:bg-muted/50 hover:text-foreground rounded-md transition-all duration-200"
                      >
                        Papers
                      </Link>
                    </NavigationMenuLink>
                    <NavigationMenuLink asChild>
                      <Link
                          to="/publications/articles"
                          className="flex items-center h-9 px-3 text-sm font-medium text-popover-foreground hover:bg-muted/50 hover:text-foreground rounded-md transition-all duration-200"
                      >
                        Articles
                      </Link>
                    </NavigationMenuLink>
                    <NavigationMenuLink asChild>
                      <Link
                          to="/publications/blogs"
                          className="flex items-center h-9 px-3 text-sm font-medium text-popover-foreground hover:bg-muted/50 hover:text-foreground rounded-md transition-all duration-200"
                      >
                        Blogs
                      </Link>
                    </NavigationMenuLink>
                  </div>
                </NavigationMenuContent>
              </NavigationMenuItem>

              <NavigationMenuItem>
                <NavigationMenuLink asChild>
                  <Link
                      to="/collections"
                      className="flex items-center h-9 px-3 text-sm font-medium text-foreground hover:bg-muted/60 hover:text-foreground rounded-md transition-all duration-200"
                  >
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
                  <button className="rounded-full hover:ring-2 hover:ring-ring/20 transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-ring">
                    <Avatar className="hover:scale-105 transition-transform duration-200">
                      <AvatarFallback firstName={user.firstName} lastName={user.lastName} />
                    </Avatar>
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <DropdownMenuItem asChild>
                    <Link to="/profile" className="cursor-pointer">
                      My Profile
                    </Link>
                  </DropdownMenuItem>

                  {role === "AUTHOR" && (
                      <DropdownMenuItem asChild>
                        <Link to="/my-publications" className="cursor-pointer">
                          My Publications
                        </Link>
                      </DropdownMenuItem>
                  )}

                  {(role === "READER" || role === "AUTHOR") && (
                      <DropdownMenuItem asChild>
                        <Link to="/my-collections" className="cursor-pointer">
                          My Collections
                        </Link>
                      </DropdownMenuItem>
                  )}

                  <DropdownMenuItem
                      onSelect={signOut}
                      className="text-destructive hover:bg-destructive/10 hover:text-destructive cursor-pointer"
                  >
                    Sign Out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
          )}
        </div>
      </nav>
  )
}