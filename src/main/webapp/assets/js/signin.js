document.addEventListener("DOMContentLoaded", function() {
  const signinForm = document.getElementById("signin-form");
  const submitButton = signinForm.querySelector("button[type=submit]");

  const usernameOrEmailInput = document.getElementById("usernameOrEmail");
  const passwordInput = document.getElementById("password");

  function checkFormValidity() {
    const isUsernameValid = usernameOrEmailInput.value.trim() !== '';
    const isPasswordValid = passwordInput.value.trim() !== '';

    submitButton.disabled = !(isUsernameValid && isPasswordValid);
  }

  usernameOrEmailInput.addEventListener('input', checkFormValidity);
  passwordInput.addEventListener('input', checkFormValidity);
});