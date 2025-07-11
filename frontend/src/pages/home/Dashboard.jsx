import React, { useState, useEffect } from 'react';
import { useApp } from '../../AppContext';
import { Container, Card, Spinner, Alert } from 'react-bootstrap';

const DashboardPage = () => {
  const { user } = useApp();

  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const response = await fetch('/api/dashboard');
        if (!response.ok) {
          throw new Error('Could not fetch dashboard data. Please try logging in again.');
        }
        const data = await response.json();
        setDashboardData(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
        <Container className="text-center mt-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </Container>
    );
  }

  return (
      <Container className="mt-4">
        <Card>
          <Card.Header as="h2">Dashboard</Card.Header>
          <Card.Body>
            <Card.Title>Welcome back, {user?.firstName || 'User'}!</Card.Title>
            {error && <Alert variant="danger">{error}</Alert>}

            {dashboardData && (
                <div className="mt-4">
                  <h4>Your Personalized Feed:</h4>
                  <p>{dashboardData.message}</p>
                </div>
            )}
          </Card.Body>
        </Card>
      </Container>
  );
};

export default DashboardPage;
