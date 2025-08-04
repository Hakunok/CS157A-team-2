import { Navigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import LoadingOverlay from "@/components/shared/LoadingOverlay"

export default function PublicOnlyRoute({ children }) {
  const { user, loading } = useAuth()

  if (loading) return <LoadingOverlay />

  if (user) return <Navigate to="/" replace />

  return children
}