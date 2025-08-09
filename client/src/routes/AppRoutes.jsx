import { Routes, Route } from "react-router-dom"
import HomePage from "@/pages/HomePage"
import SignInPage from "@/pages/authpages/SignInPage.jsx"
import SignUpPage from "@/pages/authpages/SignUpPage.jsx"
import ProfilePage from "@/pages/accountpages/ProfilePage.jsx"
import MyPublicationsPage from "@/pages/publicationpages/MyPublicationsPage.jsx"
import DraftFormPage from "@/pages/publicationpages/DraftFormPage.jsx"
import AdminDashboardPage from "@/pages/accountpages/AdminDashboardPage.jsx"
import PublicOnlyRoute from "@/routes/PublicOnlyRoute"
import PrivateRoute from "@/routes/PrivateRoute"
import PublishDraftPage from "@/pages/publicationpages/PublishDraftPage.jsx";
import PublicationPage from "@/pages/publicationpages/PublicationPage.jsx"
import PublicationExplorePage from "@/pages/publicationpages/PublicationExplorePage.jsx";
import MyCollectionsPage from "@/pages/collectionpages/MyCollectionsPage.jsx";
import CollectionFormPage from "@/pages/collectionpages/CollectionFormPage.jsx";
import CollectionPage from "@/pages/collectionpages/CollectionPage.jsx";
import CollectionExplorePage from "@/pages/collectionpages/CollectionExplorePage.jsx";

export default function AppRoutes() {
  return (
      <Routes>
        {/* homepage */}
        <Route path="/" element={<HomePage />} />
        {/* user profile routes */}
        <Route path="/admin" element={<PrivateRoute adminOnly><AdminDashboardPage /></PrivateRoute>}/>
        <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>}/>
        {/* auth routes */}
        <Route path="/signin" element={<PublicOnlyRoute><SignInPage /></PublicOnlyRoute>}/>
        <Route path="/signup" element={<PublicOnlyRoute><SignUpPage /></PublicOnlyRoute>}/>
        {/* publication routes */}
        <Route path="/publications" element={<PublicationExplorePage />} />
        <Route path="/papers" element={<PublicationExplorePage kinds={["PAPER"]} />} />
        <Route path="/blogs" element={<PublicationExplorePage kinds={["BLOG", "ARTICLE"]} />} />
        <Route path="/publications/:pubId" element={<PublicationPage />} />
        <Route path="/my-publications" element={<PrivateRoute roles={["AUTHOR"]}><MyPublicationsPage /></PrivateRoute>}/>
        <Route path="/my-publications/new" element={ <PrivateRoute roles={["AUTHOR"]}><DraftFormPage /></PrivateRoute>}/>
        <Route path="/my-publications/:pubId/edit" element={ <PrivateRoute roles={["AUTHOR"]}><DraftFormPage /></PrivateRoute>}/>
        <Route path="/my-publications/:pubId/publish" element={<PrivateRoute roles={["AUTHOR"]}><PublishDraftPage /></PrivateRoute>}/>
        {/* collection routes */}
        <Route path="/my-collections" element={ <PrivateRoute roles={["AUTHOR", "READER"]}><MyCollectionsPage /></PrivateRoute>}/>
        <Route path="/my-collections/new" element={ <PrivateRoute roles={["AUTHOR", "READER"]}><CollectionFormPage /></PrivateRoute>}/>
        <Route path="/my-collections/:collectionId/edit" element={ <PrivateRoute roles={["AUTHOR", "READER"]}><CollectionFormPage /></PrivateRoute>}/>
        <Route path="/collections/:collectionId" element={<CollectionPage />} />
        <Route path="/collections" element={<CollectionExplorePage />} />
      </Routes>
  )
}