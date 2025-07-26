import { useAuth } from "@/hooks/useAuth"

export function usePermission() {
  const { permission } = useAuth()

  return {
    isReader: permission === "READER",
    isAuthor: permission === "AUTHOR",
    isAdmin: permission === "ADMIN",
  }
}

