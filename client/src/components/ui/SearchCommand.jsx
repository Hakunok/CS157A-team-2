"use client"

import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { SearchIcon } from "lucide-react"
import {
  CommandDialog,
  CommandInput,
  CommandList,
  CommandItem,
  CommandEmpty,
} from "@/components/ui/command"
import { Button } from "@/components/ui/button"
import { publicationApi } from "@/lib/api"

export function SearchCommand() {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const trimmed = query.trim()
    if (!open || trimmed.length < 1) {
      setResults([])
      return
    }

    setLoading(true)
    const timeout = setTimeout(() => {
      publicationApi
      .search(trimmed)
      .then(setResults)
      .catch(() => setResults([]))
      .finally(() => setLoading(false))
    }, 300)

    return () => clearTimeout(timeout)
  }, [query, open])

  return (
      <>
        <div
            onClick={() => setOpen(true)}
            className="h-8 px-2.5 flex items-center gap-2 rounded-[var(--radius-sm)] border border-[var(--color-border)] bg-[var(--color-background)] text-xs text-[var(--color-muted-foreground)] hover:bg-[var(--color-muted)]/40 transition cursor-pointer"
        >
          <SearchIcon className="size-3 opacity-60" />
          <span className="font-medium">Search</span>
        </div>
        <CommandDialog
            open={open}
            onOpenChange={(val) => {
              setOpen(val)
              if (!val) {
                setQuery("")
                setResults([])
              }
            }}
        >
          <CommandInput
              placeholder="Search publications..."
              value={query}
              onValueChange={setQuery}
          />
          <CommandList>
            <CommandEmpty>
              {loading ? "Searching..." : "No publications found."}
            </CommandEmpty>
            {results.map((pub) => (
                <CommandItem
                    key={pub.pubId}
                    value={pub.title}
                    onSelect={() => {
                      setOpen(false)
                      navigate(`/publications/${pub.pubId}`)
                    }}
                >
                  <span className="hover:underline underline-offset-2 text-[var(--color-foreground)] transition-colors">
                    {pub.title}
                  </span>
                </CommandItem>

            ))}
          </CommandList>
        </CommandDialog>
      </>
  )
}