package com.airchive.service;

import com.airchive.dao.UserDAO;
import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.UserNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.model.User;
import com.airchive.util.PasswordUtils;
import com.airchive.util.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The UserService class provides functionality for user management in the system.
 * Including operations such as creating users, authenticating users, updating user details,
 * managing passwords, and checking the availability of usernames and emails. The service
 * interacts with the UserDAO.
 */
public class UserService {
  private static final Logger logger = Logger.getLogger(UserService.class.getName());
  private final UserDAO userDAO;

  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
    if (this.userDAO == null) {
      throw new IllegalArgumentException("UserDAO cannot be null.");
    }
  }

  /**
   * Creates a new user with the specified details, validates the input,
   * checks for existing users, hashes the password, and saves the user
   * to the database.
   *
   * @param username the username of the user to create (must be unique)
   * @param firstName the first name of the user
   * @param lastName the last name of the user
   * @param email the email address of the user (must be unique and valid)
   * @param plainPassword the plain-text password of the user, which will be hashed
   * @return the newly created User object with an assigned ID
   * @throws ValidationException if any input parameter is invalid
   * @throws UserAlreadyExistsException if a user with the same username or email already exists
   */
  public User createUser(String username, String firstName, String lastName, String email,
      String plainPassword) throws ValidationException, UserAlreadyExistsException {
    logger.info("Creating new user: " + username);

    validateUserCreationInput(username, firstName, lastName, email, plainPassword);

    username = username.trim();
    firstName = firstName.trim();
    lastName = lastName.trim();
    email = email.trim().toLowerCase();

    checkForExistingUser(username, email);

    User user = new User();
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPasswordHash(PasswordUtils.hashPassword(plainPassword));
    user.setCreatedAt(LocalDateTime.now());

    try {
      User savedUser = userDAO.save(user);
      logger.info("Created new user: " + savedUser.getUsername());
      return savedUser;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to create user: " + username, e);
      throw new RuntimeException("Failed to create user account");
    }
  }

  /**
   * Authenticates a user using their username or email and a plain-text password.
   * This method verifies the user's credentials and returns the corresponding User object if authentication is successful.
   *
   * @param usernameOrEmail the username or email address provided by the user for authentication
   * @param plainPassword the plain-text password provided by the user for authentication
   * @return the authenticated User object containing the user's information
   * @throws ValidationException if the username/email or password is invalid, incorrect, or if the user does not exist
   */
  public User authenticateUser(String usernameOrEmail, String plainPassword) throws ValidationException {
    if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
      throw new ValidationException("Username or email is required");
    }

    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new ValidationException("Password is required");
    }

    usernameOrEmail = usernameOrEmail.trim();

    Optional<User> userOpt = userDAO.findByUsername(usernameOrEmail);
    if (userOpt.isEmpty()) {
      userOpt = userDAO.findByEmail(usernameOrEmail);
    }

    if (userOpt.isEmpty()) {
      throw new ValidationException("Invalid username or email");
    }

    User user = userOpt.get();
    if (!PasswordUtils.verifyPassword(plainPassword, user.getPasswordHash())) {
      throw new ValidationException("Invalid username/email or password");
    }

    logger.info("User authenticated successfully: " + user.getUsername());
    return user;
  }

  /**
   * Updates an existing user with the specified details. Validates the inputs,
   * checks for conflicts with existing users, trims and sanitizes the fields,
   * and persists the updated information to the database.
   *
   * @param userId the unique ID of the user to be updated; must not be null
   * @param username the new username for the user; must be unique and meet validation criteria
   * @param firstName the updated first name for the user
   * @param lastName the updated last name for the user
   * @param email the updated email address for the user; must be unique and valid
   * @return the updated User object containing the new details
   * @throws IllegalArgumentException if the userId is null
   * @throws UserNotFoundException if no user exists with the specified userId
   * @throws ValidationException if any of the input parameters fail validation checks
   * @throws UserAlreadyExistsException if the provided username or email conflicts with another existing user
   */
  public User updateUser(Integer userId, String username, String firstName, String lastName,
      String email) throws UserNotFoundException, ValidationException, UserAlreadyExistsException {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null.");
    }

    Optional<User> existingUserOpt = userDAO.findById(userId);
    if (existingUserOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with ID:" + userId);
    }

    validateUserUpdateInput(username, firstName, lastName, email);
    username = username.trim();
    firstName = firstName.trim();
    lastName = lastName.trim();
    email = email.trim().toLowerCase();

    checkForExistingUserExcept(username, email, userId);

    User existingUser = existingUserOpt.get();
    existingUser.setUsername(username);
    existingUser.setFirstName(firstName);
    existingUser.setLastName(lastName);
    existingUser.setEmail(email);

    try {
      User updatedUser = userDAO.update(existingUser);
      logger.info("User updated successfully: " + updatedUser.getUsername());
      return updatedUser;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to update user: " + userId, e);
      throw new RuntimeException("Failed to update user account", e);
    }
  }

  /**
   * Changes the password of a User identified via user ID.
   *
   * @param userId the unique identifier of the user whose password is being changed; must not be null
   * @param currentPassword the current password of the user, used for verification
   * @param newPassword the new password to replace the current password; must be at least 8 characters long
   * @throws UserNotFoundException if no user is found with the given userId
   * @throws ValidationException if the current password is incorrect, or if the new password does
   * not meet the required criteria
   * @throws IllegalArgumentException if the userId is null
   * @throws RuntimeException if an error occurs while updating the password in the system
   */
  public void changePassword(Integer userId, String currentPassword, String newPassword)
      throws UserNotFoundException, ValidationException {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    Optional<User> userOpt = userDAO.findById(userId);
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with ID:" + userId);
    }

    User user = userOpt.get();

    if (!PasswordUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
      throw new ValidationException("Current password is incorrect");
    }

    if (newPassword == null || newPassword.length() < 8) {
      throw new ValidationException("New password must be at least 8 characters long");
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

  /**
   * Retrieves a user by their username from the database.
   * The method trims the provided username and performs a search.
   * If the username is null, empty, or the user is not found, appropriate exceptions are thrown.
   *
   * @param username the username of the user to be retrieved; must not be null or empty
   * @return the User object associated with the given username
   * @throws IllegalArgumentException if the provided username is null or empty
   * @throws UserNotFoundException if no user is found with the provided username
   */
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

  /**
   * Retrieves a user by their email address.
   *
   * @param email the email address of the user to be retrieved; must not be null or empty
   * @return the User object associated with the provided email address
   * @throws IllegalArgumentException if the provided email is null or empty
   * @throws UserNotFoundException if no user is found with the provided email
   */
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

  /**
   * Checks whether the specified username is available for use.
   * This method trims the username, validates the input, and queries the database to determine if the username already exists.
   *
   * @param username the username to check for availability (must not be null or empty)
   * @return true if the username is available, false if it already exists or the input is invalid
   */
  public boolean isUsernameAvailable(String username) {
    if (username == null || username.trim().isEmpty()) {
      return false;
    }
    return !userDAO.existsByUsername(username.trim());
  }

  /**
   * Checks whether the provided email is available for use.
   * This method verifies if the email already exists in the system, ignoring case and whitespace.
   *
   * @param email the email address to check (must not be null or empty)
   * @return true if the email is available, false if it already exists or the input is invalid
   */
  public boolean isEmailAvailable(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return !userDAO.existsByEmail(email.trim().toLowerCase());
  }


  // --- Private helper methods ---

  private void validateUserCreationInput(String username, String firstName, String lastName,
      String email, String plainPassword) throws ValidationException {
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username is required");
    }
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new ValidationException("First name is required");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new ValidationException("Last name is required");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email is required");
    }
    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new ValidationException("Password is required");
    }

    username = username.trim();
    email = email.trim();

    if (!ValidationUtils.isValidUsernameLength(username)) {
      throw new ValidationException("Username must be between 3 and 20 characters long");
    }

    if (!ValidationUtils.isValidUsernameFormat(username)) {
      throw new ValidationException("Username may only contain alphanumeric characters or single underscores, and cannot begin or end with an underscore.");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      throw new ValidationException("Please enter a valid email haha.");
    }

    if (plainPassword.length() < 8) {
      throw new ValidationException("Password must be at least 8 characters long");
    }
  }

  private void validateUserUpdateInput(String username, String firstName, String lastName,
      String email) throws ValidationException {
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username is required");
    }
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new ValidationException("First name is required");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new ValidationException("Last name is required");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email is required");
    }

    username = username.trim();
    email = email.trim();

    if (!ValidationUtils.isValidUsernameLength(username)) {
      throw new ValidationException("Username must be between 3 and 20 characters long");
    }

    if (!ValidationUtils.isValidUsernameFormat(username)) {
      throw new ValidationException("Username may only contain alphanumeric characters or single underscores, and cannot begin or end with an underscore.");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      throw new ValidationException("Please enter a valid email address.");
    }
  }

  private void checkForExistingUser(String username, String email) throws UserAlreadyExistsException {
    if (userDAO.existsByUsername(username)) {
      throw new UserAlreadyExistsException("Username already exists: " + username);
    }

    if (userDAO.existsByEmail(email)) {
      throw new UserAlreadyExistsException("Email already exists: " + email);
    }
  }

  private void checkForExistingUserExcept(String username, String email, Integer excludeUserId) throws UserAlreadyExistsException {
    Optional<User> userByUsername = userDAO.findByUsername(username);
    if (userByUsername.isPresent() && !userByUsername.get().getUserId().equals(excludeUserId)) {
      throw new UserAlreadyExistsException("Username already exists: " + username);
    }

    Optional<User> userByEmail = userDAO.findByEmail(email);
    if (userByEmail.isPresent() && !userByEmail.get().getUserId().equals(excludeUserId)) {
      throw new UserAlreadyExistsException("Email already exists: " + email);
    }
  }
}
