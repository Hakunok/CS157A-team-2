import { createContext, useState, useEffect } from "react"
import { authApi } from "@/api/authApi"
import { userApi } from "@/api/userApi"

export const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [permission, setPermission] = useState(null)
  const [loading, setLoading] = useState(true)
  const [authError, setAuthError] = useState(null)

  const isAuthenticated = !!user

  async function signIn(credentials) {
    try {
      setAuthError(null)
      await authApi.signIn(credentials)
      await restoreSession()
    } catch (err) {
      const message = err?.response?.data?.message || "Failed to sign in"
      setAuthError(message)
      throw message
    }
  }

  async function signUp(data) {
    try {
      setAuthError(null)
      await authApi.signUp(data)
      await restoreSession()
    } catch (err) {
      const message = err?.response?.data?.message || "Failed to sign up"
      setAuthError(message)
      throw message
    }
  }

  async function signOut() {
    try {
      await authApi.signOut()
    } finally {
      setUser(null)
      setPermission(null)
    }
  }

  async function restoreSession() {
    try {
      const user = await userApi.getMe()
      setUser(user)
      setPermission(user.permission)
    } catch {
      setUser(null)
      setPermission(null)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    restoreSession()
  }, [])

  return (
      <AuthContext.Provider
          value={{
            user,
            permission,
            isAuthenticated,
            signIn,
            signUp,
            signOut,
            restoreSession,
            loading,
            authError,
            setAuthError,
          }}
      >
        {children}
      </AuthContext.Provider>
  )
}
