import { Navigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import LoadingOverlay from "@/components/LoadingOverlay.jsx"

export default function PublicOnlyRoute({ children }) {
  const { user, loading } = useAuth()

  if (loading) return <LoadingOverlay />

  if (user) return <Navigate to="/" replace />

  return children
}