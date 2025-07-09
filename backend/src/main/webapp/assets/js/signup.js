// This script provides validation for the signup form by communicating with
// the API endpoints in SignupServlet.
document.addEventListener('DOMContentLoaded', function() {
  const signupForm = document.getElementById('signup-form');

  const contextPath = signupForm.dataset.contextPath;

  const submitButton = signupForm.querySelector('button[type="submit"]');

  const usernameInput = document.getElementById('username');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirmPassword');

  const usernameMessage = document.getElementById('username-validation-message');
  const emailMessage = document.getElementById('email-validation-message');
  const passwordMatchMessage = document.getElementById('password-match-message');

  let isUsernameValid = false;
  let isEmailValid = false;
  let arePasswordsMatch = false;

  function validateForm() {
    submitButton.disabled = !(isUsernameValid && isEmailValid && arePasswordsMatch);
  }

  function validateUsernameInput() {
    const username = this.value.trim();
    isUsernameValid = false;

    if (username.length >= 3 && username.length <= 20) {

      // send to servlet for validation
      fetch(`${contextPath}/api/check-username?username=` + encodeURIComponent(username))
      .then(response => response.json())
      .then(data => {
        usernameMessage.textContent = data.message;
        usernameMessage.style.color = data.available ? 'green' : 'red';
        isUsernameValid = data.available;
      })
      .catch(error => {
        console.error('Error checking username:', error);
        usernameMessage.textContent = 'Could not validate username with server.';
        usernameMessage.style.color = 'red';
      })
      .finally(() => {
        validateForm();
      });
    } else {
      usernameMessage.textContent = 'Username must be between 3 and 20 characters.';
      usernameMessage.style.color = 'red';
      validateForm();
    }
  }

  function validateEmailInput() {
    const email = this.value.trim();
    isEmailValid = false;

    if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      
      // If it passes at least the preliminary regex formatting then send to
      // servlet for further validation
      fetch(`${contextPath}/api/check-email?email=` + encodeURIComponent(email))
      .then(response => response.json())
      .then(data => {
        emailMessage.textContent = data.message;
        emailMessage.style.color = data.available ? 'green' : 'red';
        isEmailValid = data.available;
      })
      .catch(error => {
        console.error('Error checking email:', error);
        emailMessage.textContent = 'Could not validate email with server.';
        emailMessage.style.color = 'red';
      })
      .finally(() => {
        validateForm();
      });
    } else if (email.length > 0) {
      emailMessage.textContent = 'Please enter a valid email.';
      emailMessage.style.color = 'red';
      validateForm();
    } else {
      emailMessage.textContent = 'Email field cannot be left blank.';
      emailMessage.style.color = 'red';
      validateForm();
    }
  }

  function validatePasswordMatch() {
    if (passwordInput.value && confirmPasswordInput.value) {
      if (passwordInput.value !== confirmPasswordInput.value) {
        passwordMatchMessage.textContent = 'Passwords do not match.';
        passwordMatchMessage.style.color = 'red';
        arePasswordsMatch = false;
      } else {
        passwordMatchMessage.textContent = 'Passwords match.';
        passwordMatchMessage.style.color = 'green';
        arePasswordsMatch = true;
      }
    } else {
      passwordMatchMessage.textContent = '';
      arePasswordsMatch = false;
    }
    validateForm();
  }

  usernameInput.addEventListener('input', validateUsernameInput);
  emailInput.addEventListener('input', validateEmailInput);
  confirmPasswordInput.addEventListener('input', validatePasswordMatch);
  passwordInput.addEventListener('input', validatePasswordMatch);

  validateForm();
});