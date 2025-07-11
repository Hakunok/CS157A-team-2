import React, { useEffect } from 'react';
import { Routes, Route, useNavigate } from "react-router-dom";
import { AppProvider, useApp } from './AppContext';

// Layout and Routing Components
import MainLayout from './components/layout/MainLayout';
import PrivateRoute from './components/router/PrivateRoute';

// Page Components
import HomePage from './pages/home/HomePage';
import DashboardPage from './pages/home/Dashboard';
import SigninPage from './pages/authentication/SigninPage';
import SignupPage from './pages/authentication/SignupPage';

// --- Redirect Logic for the Homepage ---
// This component checks if a user is logged in and redirects them to the dashboard if so.
const HomeRedirect = () => {
  const { user, loading } = useApp();
  const navigate = useNavigate();

  useEffect(() => {
    // Wait for the initial session check to complete before redirecting
    if (!loading && user) {
      navigate('/dashboard', { replace: true });
    }
  }, [user, loading, navigate]);

  // While loading or if no user, show the public homepage.
  return <HomePage />;
};


// --- Main App Component ---
// The BrowserRouter has been removed from this file.
// It will now be wrapped around <App /> in main.jsx.
function App() {
  return (
      <AppProvider>
        <Routes>
          {/* Routes WITHOUT the main navbar */}
          <Route path="/signin" element={<SigninPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Routes that ARE WRAPPED by the MainLayout (and thus have a navbar) */}
          <Route element={<MainLayout />}>
            <Route path="/" element={<HomeRedirect />} />

            {/* Private Route for the dashboard */}
            <Route
                path="/dashboard"
                element={
                  <PrivateRoute>
                    <DashboardPage />
                  </PrivateRoute>
                }
            />
          </Route>
        </Routes>
      </AppProvider>
  );
}

export default App;
