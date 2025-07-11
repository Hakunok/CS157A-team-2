import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../../AppContext';
import { Navbar, Nav, NavDropdown, Container, Form } from 'react-bootstrap';
// LinkContainer is no longer needed
// import { LinkContainer } from 'react-router-bootstrap';

const AppNavbar = () => {
  const { user, logout } = useApp();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/');
    } catch (error) {
      console.error("Failed to sign out", error);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  return (
      // The bg="dark" prop ensures the navbar has the correct dark background
      <Navbar bg="dark" variant="dark" expand="lg" collapseOnSelect>
        <Container>
          {/* FIX: Replaced LinkContainer with the 'as={Link}' prop */}
          <Navbar.Brand as={Link} to={user ? '/dashboard' : '/'}>
            aiRchive
          </Navbar.Brand>

          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="me-auto">
              {/* Add links to publications, etc. here later */}
            </Nav>

            <Form className="d-flex my-2 my-lg-0" onSubmit={handleSearchSubmit}>
              <Form.Control
                  type="search"
                  placeholder="Search..."
                  className="me-2"
                  aria-label="Search"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
              />
            </Form>

            <Nav className="ms-auto align-items-center">
              {/* The theme toggle button has been removed */}
              {user ? (
                  <NavDropdown title={user.username} id="basic-nav-dropdown">
                    {/* Add dropdown items here later */}
                    <NavDropdown.Divider />
                    <NavDropdown.Item as="button" onClick={handleLogout}>
                      Sign Out
                    </NavDropdown.Item>
                  </NavDropdown>
              ) : (
                  <>
                    {/* FIX: Replaced LinkContainer with the 'as={Link}' prop */}
                    <Nav.Link as={Link} to="/signin">Sign In</Nav.Link>
                    <Nav.Link as={Link} to="/signup">Sign Up</Nav.Link>
                  </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
  );
};

export default AppNavbar;