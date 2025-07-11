import React from 'react';
import { Outlet } from 'react-router-dom';
import AppNavbar from '../navbar/Navbar';

const MainLayout = () => {
  return (
      <>
        <AppNavbar />
        <main>
          <Outlet />
        </main>
      </>
  );
};

export default MainLayout;