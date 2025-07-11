import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Button, Card } from 'react-bootstrap';

const HomePage = () => {
  return (
      <Container className="d-flex align-items-center justify-content-center text-center" style={{ minHeight: '80vh' }}>
        <div>
          <Card className="p-5">
            <Card.Body>
              <h1 className="display-4">Welcome to aiRchive</h1>
              <p className="lead">
                Your personal archive for academic papers and collections.
              </p>
              <hr className="my-4" />
              <p>
                Join our community of readers and authors. Sign up to get started or sign in to access your dashboard.
              </p>
              <p className="lead">
                <Button as={Link} to="/signin" variant="primary" size="lg" className="me-2">Sign In</Button>
                <Button as={Link} to="/signup" variant="secondary" size="lg">Sign Up</Button>
              </p>
            </Card.Body>
          </Card>
        </div>
      </Container>
  );
};

export default HomePage;