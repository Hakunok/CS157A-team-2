// src/routes/routes.jsx
import { Route, Routes } from "react-router-dom"
import HomePage from "@/pages/Home"
import SignIn from "@/pages/SignIn"
import SignUp from "@/pages/SignUp"
import ProfilePage from "@/pages/ProfilePage"
import MyPublications from "@/pages/MyPublications"
import CreatePublication from "@/pages/CreatePublication"
import AdminDashboard from "@/pages/AdminDashboard"
import RequireAuth from "@/guards/RequireAuth"

export default function AppRoutes() {
  return (
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/signin" element={<SignIn />} />
        <Route path="/signup" element={<SignUp />} />

        {/* Protected Routes */}
        <Route
            path="/profile"
            element={
              <RequireAuth>
                <ProfilePage />
              </RequireAuth>
            }
        />

        <Route
            path="/my-publications"
            element={
              <RequireAuth allowed={["AUTHOR"]}>
                <MyPublications />
              </RequireAuth>
            }
        />

        <Route
            path="/create-publication"
            element={
              <RequireAuth allowed={["AUTHOR"]}>
                <CreatePublication />
              </RequireAuth>
            }
        />

        <Route
            path="/admin"
            element={
              <RequireAuth allowed={["ADMIN"]}>
                <AdminDashboard />
              </RequireAuth>
            }
        />
      </Routes>
  )
}
