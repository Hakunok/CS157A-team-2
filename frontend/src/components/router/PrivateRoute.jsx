import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useApp } from '../../AppContext';
import LoadingSpinner from "../spinner/LoadingSpinner";


const PrivateRoute = ({ children }) => {
  const { user, loading } = useApp();

  const location = useLocation();

  if (loading) {
    return LoadingSpinner();
  }

  if (user) {
    return children;
  }

  return <Navigate to="/signin" state={{ from: location }} replace />;
};

export default PrivateRoute;