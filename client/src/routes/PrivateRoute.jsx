import { Navigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import LoadingOverlay from "@/components/LoadingOverlay.jsx"

export default function PrivateRoute({ children, roles, adminOnly = false }) {
  const { user, loading } = useAuth()
  if (loading) return <LoadingOverlay />
  if (!user) return <Navigate to="/signin" replace />
  if (adminOnly && !user.isAdmin) {
    return <Navigate to="/" replace />
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/" replace />
  }
  return children
}