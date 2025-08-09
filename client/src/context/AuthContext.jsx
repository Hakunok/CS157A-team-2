import { createContext, useContext, useEffect, useState } from "react"
import { authApi, userApi } from "@/lib/api"

const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    authApi.refresh()
    .then((user) => {
      setUser(user ?? null)
    })
    .catch(() => {
      setUser(null)
    })
    .finally(() => {
      setLoading(false)
    })
  }, [])

  const login = async (credentials) => {
    const user = await authApi.login(credentials)
    setUser(user)
    userApi.recalculateAffinities().catch(err => {
      console.error("Affinity recalculation trigger failed:", err)
    })
    return user
  }
  const register = async (data) => {
    const user = await authApi.register(data)
    setUser(user)
    return user
  }
  const logout = async () => {
    await authApi.logout()
    setUser(null)
  }
  const isAuthenticated = !!user
  const isAdmin = user?.isAdmin || false
  const isAuthor = user?.role === "AUTHOR"
  const isReader = user?.role === "READER"

  return (
      <AuthContext.Provider value={{ user, isAuthenticated, isAdmin, isAuthor, isReader, login, register, logout, loading }}>
        {children}
      </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}