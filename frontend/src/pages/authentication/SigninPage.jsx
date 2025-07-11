import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useApp } from '../../AppContext';
import { Form, Button, Container, Card, Alert, FloatingLabel } from 'react-bootstrap';

const SignInPage = () => {
  const { login } = useApp();
  const navigate = useNavigate();
  const location = useLocation();

  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [validated, setValidated] = useState(false);

  const from = location.state?.from?.pathname || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setValidated(true);

    if (!usernameOrEmail || !password) {
      return;
    }

    setError('');
    setLoading(true);

    try {
      await login(usernameOrEmail, password);

      navigate(from, { replace: true });
    } catch (err) {
      setError('Failed to sign in. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
      <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh' }}>
        <Card className="border-0" style={{ width: '400px' }}>
          <Card.Body>
            <h2 className="text-center mb-4">Sign In</h2>
            {error && <Alert variant="danger">{error}</Alert>}

            <Form noValidate onSubmit={handleSubmit}>
              <FloatingLabel
                  controlId="floatingInput"
                  label="Username or Email"
                  className="mb-3"
              >
                <Form.Control
                    type="text"
                    placeholder="name@example.com"
                    value={usernameOrEmail}
                    onChange={(e) => setUsernameOrEmail(e.target.value)}
                    isInvalid={validated && !usernameOrEmail}
                />
              </FloatingLabel>

              <FloatingLabel
                  controlId="floatingPassword"
                  label="Password"
                  className="mb-3"
              >
                <Form.Control
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    isInvalid={validated && !password}
                />
              </FloatingLabel>

              <Button variant="primary" disabled={loading} className="w-100 mt-3" type="submit">
                {loading ? 'Signing In…' : 'Sign In'}
              </Button>
            </Form>
            <div className="w-100 text-center mt-3">
              New to aiRchive? <Link to="/signup">Sign up</Link>
            </div>
          </Card.Body>
        </Card>
      </Container>
  );
};

export default SignInPage;