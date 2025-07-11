import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from './services/authService';

const AppContext = createContext(null);

export const AppProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // --- THIS IS THE FIX ---
  // This effect runs only once when the app loads.
  // It permanently sets the theme for the entire application to dark mode.
  useEffect(() => {
    document.documentElement.setAttribute('data-bs-theme', 'dark');
  }, []);

  // Check for an active session when the app first loads
  useEffect(() => {
    const checkLoggedIn = async () => {
      try {
        const currentUser = await authService.status();
        setUser(currentUser);
      } catch (error) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    checkLoggedIn();
  }, []);

  const login = async (email, password) => {
    const loggedInUser = await authService.login(email, password);
    setUser(loggedInUser);
    return loggedInUser;
  };

  const register = async (userData) => {
    const newUser = await authService.register(userData);
    setUser(newUser);
    return newUser;
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  // The value no longer needs to include theme-related functions
  const value = {
    user,
    loading,
    login,
    register,
    logout,
  };

  return (
      <AppContext.Provider value={value}>
        {!loading && children}
      </AppContext.Provider>
  );
};

export const useApp = () => {
  return useContext(AppContext);
};
