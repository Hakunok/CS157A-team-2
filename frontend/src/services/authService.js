const API_BASE_URL = 'http://localhost:8080/airchive_war_exploded/api';

/**
 * A centralized helper function for making API calls to the backend.
 * It automatically includes credentials (like session cookies) for every request.
 * @param {string} endpoint - The API endpoint to call (e.g., '/auth/login').
 * @param {object} options - The options for the fetch call (method, headers, body).
 * @returns {Promise<object>} The JSON response from the server.
 */
const apiFetch = async (endpoint, options = {}) => {
  options.credentials = 'include';

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`;
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || JSON.stringify(errorData);
      } catch (e) {
      }
      throw new Error(errorMessage);
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  } catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error('Unable to connect to server. Please check if the backend is running.');
    }
    throw error;
  }
};


const login = (usernameOrEmail, password) => {
  return apiFetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ usernameOrEmail, password }),
  });
};

const register = (userData) => {
  return apiFetch('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
  });
};

const logout = () => {
  return apiFetch('/auth/logout', { method: 'POST' });
};

const status = () => {
  return apiFetch('/auth/status');
};

const authService = { login, register, logout, status };
export default authService;
