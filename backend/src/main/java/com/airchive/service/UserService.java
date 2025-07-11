package com.airchive.service;

import com.airchive.dao.UserDAO;
import com.airchive.exception.EmailAlreadyExistsException;
import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.UserNotFoundException;
import com.airchive.exception.UsernameAlreadyExistsException;
import com.airchive.exception.ValidationException;
import com.airchive.model.User;
import com.airchive.model.User.Role;
import com.airchive.model.User.Status;
import com.airchive.util.PasswordUtils;
import com.airchive.util.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 * The UserService class provides functionality for user management in the system.
 * Including operations such as creating users, authenticating users, updating user details,
 * managing passwords, and checking the availability of usernames and emails. The service
 * interacts with the UserDAO.
 */
public class UserService {
  private static Logger logger = Logger.getLogger(UserService.class.getName());
  private final UserDAO userDAO;

  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public UserService(ServletContext context) {
    this((UserDAO) context.getAttribute("userDAO"));
  }

  public User registerNewUser(String username, String firstName, String lastName, String email,
      String plainPassword) throws ValidationException, UserAlreadyExistsException {

    validateUserInput(username, firstName, lastName, email, plainPassword);

    String sanitizedUsername = username.trim();
    String sanitizedEmail = email.trim().toLowerCase();
    checkForExistingUser(sanitizedUsername, sanitizedEmail);

    User user = new User();
    user.setUsername(sanitizedUsername);
    user.setFirstName(firstName.trim());
    user.setLastName(lastName.trim());
    user.setEmail(sanitizedEmail);
    user.setPasswordHash(PasswordUtils.hashPassword(plainPassword));
    user.setRole(Role.READER);
    user.setStatus(Status.ACTIVE);
    user.setCreatedAt(LocalDateTime.now());

    try {
      User createdUser = userDAO.create(user);
      logger.info("Created new user: " + createdUser.getUsername());
      return createdUser;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to create user due to SQL error: " + username, e);
      throw new RuntimeException("Failed to create user account", e);
    }
  }

  public User authenticateUser(String usernameOrEmail, String plainPassword) throws ValidationException {
    if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
      throw new ValidationException("Username or email is required.");
    }

    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new ValidationException("Password is required.");
    }

    Optional<User> userOpt = userDAO.findByUsername(usernameOrEmail.trim());
    if (userOpt.isEmpty()) {
      userOpt = userDAO.findByEmail(usernameOrEmail.trim());
    }

    if (userOpt.isEmpty() || !PasswordUtils.verifyPassword(plainPassword, userOpt.get().getPasswordHash())) {
      throw new ValidationException("Invalid credentials.");
    }

    User user = userOpt.get();
    if (user.getStatus() != Status.ACTIVE) {
      throw new ValidationException("This account is currently " + user.getStatus().name().toLowerCase() + ".");
    }

    logger.info("User authenticated successfully: " + user.getUsername());
    return user;
  }


  public User updateUser(Integer userId, String username, String firstName, String lastName,
      String email, Role role, Status status) throws UserNotFoundException, ValidationException,
      UserAlreadyExistsException {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null.");
    }

    User user = userDAO.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    validateUserInput(username, firstName, lastName, email, null);

    String sanitizedUsername = username.trim();
    String sanitizedEmail = email.trim().toLowerCase();
    checkForExistingUserExcluding(sanitizedUsername, sanitizedEmail, userId);

    user.setUsername(sanitizedUsername);
    user.setFirstName(firstName.trim());
    user.setLastName(lastName.trim());
    user.setEmail(sanitizedEmail);
    user.setRole(role);
    user.setStatus(status);

    try {
      User updatedUser = userDAO.update(user);
      logger.info("Successfully updated user: " + updatedUser.getUsername());
      return updatedUser;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to update user: " + userId, e);
      throw new RuntimeException("Failed to update user account", e);
    }
  }


  public void changePassword(Integer userId, String currentPassword, String newPassword)
      throws UserNotFoundException, ValidationException {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    User user = userDAO.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!PasswordUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
      throw new ValidationException("Current password is incorrect");
    }

    if (!ValidationUtils.isValidPassword(newPassword)) {
      throw new ValidationException("Password must be at least 8 characters long.");
    }

    user.setPasswordHash(PasswordUtils.hashPassword(newPassword));

    try {
      userDAO.update(user);
      logger.info("Password changed successfully for user: " + user.getUsername());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to change password for user: " + userId, e);
      throw new RuntimeException("Failed to change password for user", e);
    }
  }

  public User findByUsername(String username) throws UserNotFoundException {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    Optional<User> userOpt = userDAO.findByUsername(username.trim());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with username: " + username);
    }

    return userOpt.get();
  }


  public User findByEmail(String email) throws UserNotFoundException {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with email: " + email);
    }

    return userOpt.get();
  }

  public boolean isUsernameAvailable(String username) {
    if (username == null || username.trim().isEmpty()) {
      return false;
    }
    return !userDAO.existsByUsername(username.trim());
  }


  public boolean isEmailAvailable(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return !userDAO.existsByEmail(email.trim().toLowerCase());
  }

  // Validation messages sent by the ValidationServlet to the React frontend
  public String validateField(String field, String value, String passwordForConfirmation) {
    try {
      switch (field) {
        case "username":
          if (!ValidationUtils.isValidUsername(value)) {
            throw new ValidationException("Username must be 3-20 characters long, with no "
                + "leading/trailing underscores.");
          }
          if (userDAO.existsByUsername(value.trim())) {
            throw new UsernameAlreadyExistsException("This username is already in use.");
          }
          break;
        case "email":
          if (!ValidationUtils.isValidEmail(value)) {
            throw new ValidationException("Please enter a valid email address.");
          }
          if (userDAO.existsByEmail(value.trim().toLowerCase())) {
            throw new EmailAlreadyExistsException("This email is already in use.");
          }
          break;
        case "password":
          if (!ValidationUtils.isValidPassword(value)) {
            throw new ValidationException("Password must be at least 8 characters long.");
          }
          break;
        case "confirmPassword":
          if (passwordForConfirmation == null || passwordForConfirmation.trim().isEmpty()) {
            throw new ValidationException("Password is required first.");
          }
          if (value == null || !value.equals(passwordForConfirmation)) {
            throw new ValidationException("Passwords must match.");
          }
          break;
        case "firstName":
          if (!ValidationUtils.isValidName(value)) {
            throw new ValidationException("First name contains invalid characters or is too long.");
          }
          break;
        case "lastName":
          if (!ValidationUtils.isValidName(value)) {
            throw new ValidationException("Last name contains invalid characters or is too long.");
          }
          break;
      }
      return null;
    } catch (ValidationException | UserAlreadyExistsException e) {
      return e.getMessage();
    }
  }


  // --- Private helper methods ---

  private void validateUserInput(String username, String firstName, String lastName,
      String email, String password) throws ValidationException {
    if (!ValidationUtils.isValidUsername(username)) {
      throw new ValidationException("Username must be 3-20 characters long, with no "
          + "leading/trailing underscores.");
    }
    if (!ValidationUtils.isValidName(firstName)) {
      throw new ValidationException("First name is required and must be 40 characters or less.");
    }
    if (!ValidationUtils.isValidName(lastName)) {
      throw new ValidationException("Last name is required and must be 40 characters or less.");
    }
    if (!ValidationUtils.isValidEmail(email)) {
      throw new ValidationException("Email is invalid.");
    }
    if (password != null && !ValidationUtils.isValidPassword(password)) {
      throw new ValidationException("Password must be at least 8 characters long.");
    }
  }

  private void checkForExistingUser(String username, String email) throws UserAlreadyExistsException {
    if (userDAO.existsByUsername(username)) {
      throw new UsernameAlreadyExistsException("Username is already taken.");
    }

    if (userDAO.existsByEmail(email)) {
      throw new EmailAlreadyExistsException("Email is already taken.");
    }
  }

  private void checkForExistingUserExcluding(String username, String email,
      Integer excludeUserId) throws UserAlreadyExistsException {
    Optional<User> userByUsername = userDAO.findByUsername(username);
    if (userByUsername.isPresent() && !userByUsername.get().getUserId().equals(excludeUserId)) {
      throw new UsernameAlreadyExistsException("Username is already taken.");
    }

    Optional<User> userByEmail = userDAO.findByEmail(email);
    if (userByEmail.isPresent() && !userByEmail.get().getUserId().equals(excludeUserId)) {
      throw new EmailAlreadyExistsException("Email is already taken.");
    }
  }
}
