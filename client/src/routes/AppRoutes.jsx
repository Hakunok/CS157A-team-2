import { Routes, Route } from "react-router-dom"
import HomePage from "@/pages/HomePage"
import SignInPage from "@/pages/authpages/SignInPage.jsx"
import SignUpPage from "@/pages/authpages/SignUpPage.jsx"
import ProfilePage from "@/pages/userpages/ProfilePage.jsx"
import MyPublicationsPage from "@/pages/publicationpages/MyPublicationsPage.jsx"
import DraftFormPage from "@/pages/publicationpages/DraftFormPage.jsx"
import AdminDashboardPage from "@/pages/userpages/AdminDashboardPage.jsx"
import PublicOnlyRoute from "@/routes/PublicOnlyRoute"
import PrivateRoute from "@/routes/PrivateRoute"
import PublishDraftPage from "@/pages/publicationpages/PublishDraftPage.jsx";
import PublicationDetailPage from "@/pages/publicationpages/PublicationDetailPage"
import PublicationExplorePage
  from "@/pages/publicationpages/PublicationExplorePage.jsx";


export default function AppRoutes() {
  return (
      <Routes>
        <Route path="/" element={<HomePage />} />

        {/* Public-only (unauthenticated) routes */}
        <Route
            path="/signin"
            element={
              <PublicOnlyRoute>
                <SignInPage />
              </PublicOnlyRoute>
            }
        />
        <Route
            path="/signup"
            element={
              <PublicOnlyRoute>
                <SignUpPage />
              </PublicOnlyRoute>
            }
        />

        <Route path="/papers" element={<PublicationExplorePage kind="PAPER" />} />
        <Route path="/blogs" element={<PublicationExplorePage kind="BLOG" />} />
        <Route path="/articles" element={<PublicationExplorePage kind="ARTICLE" />} />

        <Route
            path="/publications/:pubId"
            element={<PublicationDetailPage />}
        />

        {/* Authenticated-only routes */}
        <Route
            path="/profile"
            element={
              <PrivateRoute>
                <ProfilePage />
              </PrivateRoute>
            }
        />

        {/* Author-only routes */}
        <Route
            path="/my-publications"
            element={
              <PrivateRoute roles={["AUTHOR"]}>
                <MyPublicationsPage />
              </PrivateRoute>
            }
        />
        <Route
            path="/my-publications/new"
            element={
              <PrivateRoute roles={["AUTHOR"]}>
                <DraftFormPage />
              </PrivateRoute>
            }
        />
        <Route
            path="/my-publications/:pubId/edit"
            element={
              <PrivateRoute roles={["AUTHOR"]}>
                <DraftFormPage />
              </PrivateRoute>
            }
        />
        <Route
            path="/my-publications/:pubId/publish"
            element={
              <PrivateRoute roles={["AUTHOR"]}>
                <PublishDraftPage />
              </PrivateRoute>
            }
        />
        {/* Admin-only route */}
        <Route
            path="/admin"
            element={
              <PrivateRoute adminOnly>
                <AdminDashboardPage />
              </PrivateRoute>
            }
        />
      </Routes>
  )
}