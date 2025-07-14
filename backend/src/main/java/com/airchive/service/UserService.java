package com.airchive.service;

import com.airchive.dao.UserDAO;
import com.airchive.exception.EmailAlreadyExistsException;
import com.airchive.exception.FailedOperationException;
import com.airchive.exception.UserAlreadyExistsException;
import com.airchive.exception.UserNotFoundException;
import com.airchive.exception.UsernameAlreadyExistsException;
import com.airchive.exception.ValidationException;
import com.airchive.model.User;
import com.airchive.model.User.UserRole;
import com.airchive.model.User.UserStatus;
import com.airchive.util.ApplicationContextProvider;
import com.airchive.util.PasswordUtils;
import com.airchive.util.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 * The {@code UserService} class provides the core functionality for managing
 * user-related operations in the system. It is responsible for user registration,
 * authentication, and updating user details including username, email, role, and
 * status. This class ensures that input data is validated, conflicts are handled,
 * and user data is persisted reliably.
 */
public class UserService {
  private static final Logger logger = Logger.getLogger(UserService.class.getName());
  private final UserDAO userDAO;

  public UserService() {
    ServletContext context = ApplicationContextProvider.getServletContext();
    this.userDAO = (UserDAO) context.getAttribute("userDAO");
  }

  /**
   * Registers a new user in the system after validating the input parameters and ensuring the username and email are unique.
   *
   * @param username the desired username for the new user; must meet the required format rules
   * @param firstName the first name of the user; must not contain invalid characters or exceed the length limit
   * @param lastName the last name of the user; must not contain invalid characters or exceed the length limit
   * @param email the email address of the user; must be a valid email format
   * @param plainPassword the plain text password for the user; must meet the security requirements (e.g., minimum length)
   * @return the created User object with all properties initialized and stored in the database
   * @throws ValidationException if any input parameter fails the validation checks
   * @throws UserAlreadyExistsException if the username or email is already registered in the system
   */
  public User registerNewUser(String username, String firstName, String lastName, String email,
      String plainPassword) throws ValidationException, UserAlreadyExistsException, FailedOperationException {

    if (!ValidationUtils.isValidUsername(username)) {
      throw new ValidationException("Username can use 3–20 letters, numbers, or . _ - (no symbols at the edges or back-to-back).");
    }
    if (!ValidationUtils.isValidName(firstName)) {
      throw new ValidationException("First name contains invalid characters or is too long.");
    }
    if (!ValidationUtils.isValidName(lastName)) {
      throw new ValidationException("Last name contains invalid characters or is too long.");
    }
    if (!ValidationUtils.isValidEmail(email)) {
      throw new ValidationException("Please enter a valid email address.");
    }
    if (!ValidationUtils.isValidPassword(plainPassword)) {
      throw new ValidationException("Password must be at least 8 characters long.");
    }

    String sanitizedUsername = username.trim();
    String sanitizedEmail = email.trim().toLowerCase();
    checkForExistingUser(sanitizedUsername, sanitizedEmail, null);

    User user = new User(
        sanitizedUsername,
        firstName.trim(),
        lastName.trim(),
        sanitizedEmail,
        PasswordUtils.hashPassword(plainPassword),
        UserRole.READER,
        UserStatus.ACTIVE,
        LocalDateTime.now()
    );

    try {
      return userDAO.create(user);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to create user due to SQL error: " + username, e);
      throw new FailedOperationException("Failed to create user account", e);
    }
  }


  /**
   * Authenticates a user by verifying their credentials and returning the corresponding
   * {@code User} object if authentication is successful. This includes checking the provided
   * username or email and password, as well as ensuring the user's account is in an active state.
   *
   * @param usernameOrEmail The username or email of the user attempting authentication. This value
   *                        should not be null or empty.
   * @param plainPassword   The plain text password provided by the user for authentication. This value
   *                        should not be null or empty.
   * @return The authenticated {@code User} object associated with the provided credentials.
   * @throws ValidationException If any of the following occurs:
   *                              - Either {@code usernameOrEmail} or {@code plainPassword} is null or empty.
   *                              - No user is found with the provided username or email.
   *                              - The provided password does not match the stored password for the user.
   *                              - The user's account is not in an active state.
   */
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
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ValidationException("This account is currently " + user.getStatus().name().toLowerCase() + ".");
    }

    logger.info("User authenticated successfully: " + user.getUsername());
    return user;
  }

  /**
   * Updates the username of an existing user identified by their unique user ID.
   * This method validates the new username, ensures that it adheres to the
   * required format, and checks for conflicts with existing users in the system.
   *
   * @param userId The unique identifier of the user whose username is to be updated.
   *               Must be a valid existing user ID.
   * @param newUsername The new desired username for the user. It must be unique
   *                    and adhere to validation rules (e.g., length and format).
   * @return The updated {@code User} object with the newly assigned username.
   * @throws UserNotFoundException If no user is found with the given user ID.
   * @throws ValidationException If the new username is invalid (e.g., fails length
   *                              or format requirements).
   * @throws UserAlreadyExistsException If a user with the same username already exists.
   */
  public User updateUsername(int userId, String newUsername) throws UserNotFoundException,
      ValidationException, UserAlreadyExistsException, FailedOperationException {
    if (!ValidationUtils.isValidUsername(newUsername)) {
      throw new ValidationException("Please choose a username "
          + "that is between 3-20 characters, with no leading/trailing underscores.");
    }
    checkForExistingUser(newUsername, null, userId);

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setUsername(newUsername);
    return persistUpdate(user);
  }

  public User updateFirstName(int userId, String newFirstName)
      throws ValidationException, UserNotFoundException, FailedOperationException {
    if (!ValidationUtils.isValidName(newFirstName)) {
      throw new ValidationException("Please enter a valid first name.");
    }

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setFirstName(newFirstName.trim());
    User updated = persistUpdate(user);
    syncAuthorNameIfLinked(updated);
    return updated;
  }

  public User updateLastName(int userId, String newLastName)
      throws ValidationException, UserNotFoundException, FailedOperationException {
    if (!ValidationUtils.isValidName(newLastName)) {
      throw new ValidationException("Please enter a valid last name.");
    }

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setLastName(newLastName.trim());
    User updated = persistUpdate(user);
    syncAuthorNameIfLinked(updated);
    return updated;
  }

  /**
   * Updates the email address of a user identified by their unique user ID.
   * This method validates the new email address, ensures it is in a proper format,
   * and checks for conflicts with any existing user in the system. If validation passes,
   * the email is updated and persisted.
   *
   * @param userId The unique identifier of the user whose email address is to be updated.
   *               Must refer to an existing user in the system.
   * @param newEmail The new email address to be assigned to the user. Must be a valid email
   *                 format and unique within the system.
   * @return The updated {@code User} object with the new email address.
   * @throws ValidationException If the provided email format is invalid.
   * @throws UserAlreadyExistsException If a user with the same email address already exists.
   * @throws UserNotFoundException If no user is found with the given user ID.
   * @throws FailedOperationException If an error occurs while persisting the updated email address.
   */
  public User updateEmail(int userId, String newEmail) throws ValidationException,
      UserAlreadyExistsException, UserNotFoundException, FailedOperationException {
    if (!ValidationUtils.isValidEmail(newEmail)) {
      throw new ValidationException("Please enter a valid email address.");
    }
    checkForExistingUser(null, newEmail, userId);

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setEmail(newEmail.trim().toLowerCase());
    return persistUpdate(user);
  }

  /**
   * Updates the role of a user identified by the provided user ID.
   * Validates the new role string to ensure it corresponds to a valid role.
   *
   * @param userId the unique identifier of the user whose role is to be updated
   * @param newRoleString the new role to assign to the user, must be one of READER, AUTHOR, or ADMIN
   * @return the updated User object with the new role assigned
   * @throws UserNotFoundException if no user is found with the specified userId
   * @throws ValidationException if the provided newRoleString is null, empty, or invalid
   * @throws FailedOperationException if the update operation fails
   */
  public User updateRole(Integer userId, String newRoleString) throws UserNotFoundException,
      ValidationException, FailedOperationException {
    if (newRoleString == null || newRoleString.trim().isEmpty()) {
      throw new ValidationException("Please choose a role.");
    }

    UserRole userRole;
    try {
      userRole = UserRole.valueOf(newRoleString.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid role. Must be READER, AUTHOR, or ADMIN.");
    }

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setRole(userRole);
    return persistUpdate(user);
  }

  /**
   * Updates the status of an existing user to the specified new status.
   * This method validates the provided status string, ensures that the
   * user exists, and persists the status update.
   *
   * @param userId The unique identifier of the user whose status is to be updated. Must not be null.
   * @param newStatusString The new status to assign to the user. Accepted values are "ACTIVE",
   *                        "SUSPENDED", or "DELETED". Must not be null or empty.
   * @return The updated {@code User} object with the newly assigned status.
   * @throws UserNotFoundException If no user is found with the given userId.
   * @throws ValidationException If the provided status string is null, empty, or invalid.
   * @throws FailedOperationException If an error occurs while persisting the updated status.
   */
  public User updateStatus(Integer userId, String newStatusString) throws UserNotFoundException,
      ValidationException, FailedOperationException {
    if (newStatusString == null || newStatusString.trim().isEmpty()) {
      throw new ValidationException("Please choose a status.");
    }

    UserStatus userStatus;
    try {
      userStatus = UserStatus.valueOf(newStatusString.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid status. Must be ACTIVE, SUSPENDED, or DELETED.");
    }

    User user = userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    user.setStatus(userStatus);
    return persistUpdate(user);
  }

  /**
   * Validates the given field based on the provided type and value. Depending on the field type,
   * this method applies various validation rules such as checking format, length, uniqueness,
   * or matching related fields like password confirmation. If validation fails, a respective
   * error message is returned. If validation is successful, it returns null.
   *
   * @param field The type of field to validate (e.g., "username", "email", "password", etc.).
   *              Must not be null.
   * @param value The value to validate against the specified field. Can be null but will be
   *              validated based on the field rules.
   * @param passwordForConfirmation The password value to confirm against, applicable only for
   *                                 "confirmPassword" validation. Can be null for non-password
   *                                 related validations.
   * @return A validation error message if the provided value fails validation; otherwise, null
   *         if validation passes.
   */
  public String validateField(String field, String value, String passwordForConfirmation) {
    try {
      switch (field) {
        case "username":
          if (!ValidationUtils.isValidUsername(value)) {
            throw new ValidationException("Username can use 3–20 letters, numbers, or . _ - (no "
                + "symbols at the edges or back-to-back).");
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
            throw new ValidationException("Please choose a password first.");
          }
          if (value == null || !value.equals(passwordForConfirmation)) {
            throw new ValidationException("Passwords do not match.");
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

  // this method is not used yet. just here for the future
  public void changePassword(Integer userId, String currentPassword, String newPassword)
      throws UserNotFoundException, ValidationException, FailedOperationException {

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
      throw new FailedOperationException("Failed to change password for user", e);
    }
  }

  // this method is not used yet, just here for future need
  public User findByUsername(String username) throws IllegalArgumentException, UserNotFoundException {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    Optional<User> userOpt = userDAO.findByUsername(username.trim());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with username: " + username);
    }

    return userOpt.get();
  }

  // this method is not used yet, just here for future need
  public User findByEmail(String email) throws IllegalArgumentException, UserNotFoundException {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User not found with email: " + email);
    }

    return userOpt.get();
  }


  // --- Private helper methods ---

  /**
   * Checks for the existence of a user with the given username or email in the system,
   * excluding a specific user identified by their ID. If a user with the same username
   * or email already exists (and is not the excluded user), an exception is thrown.
   *
   * @param username The username to check for existing users. May be null, in which case
   *                 username validation is skipped.
   * @param email The email to check for existing users. May be null, in which case email
   *              validation is skipped.
   * @param excludeUserId The ID of the user to exclude from the existence check. If null,
   *                      no user is excluded.
   * @throws UserAlreadyExistsException If a user with the same username or email already
   *                                     exists (excluding the user with the given ID, if specified).
   */
  private void checkForExistingUser(String username, String email, Integer excludeUserId) throws UserAlreadyExistsException {
    if (username != null) {
      Optional<User> existingUser = userDAO.findByUsername(username);
      if (existingUser.isPresent() && (excludeUserId == null || !existingUser.get().getUserId().equals(excludeUserId))) {
        throw new UsernameAlreadyExistsException("Username is already taken.");
      }
    }

    if (email != null) {
      Optional<User> existingUser = userDAO.findByEmail(email);
      if (existingUser.isPresent() && (excludeUserId == null || !existingUser.get().getUserId().equals(excludeUserId))) {
        throw new EmailAlreadyExistsException("Email is already taken.");
      }
    }
  }

  /**
   * Persists the updated user information to the database.
   * Attempts to perform an update operation on the provided {@code User} object
   * using the user data access object (DAO). If the update fails due to a database
   * issue, a {@code FailedOperationException} is thrown with the relevant details.
   *
   * @param user The {@code User} object containing the updated information to be persisted.
   *             Must not be null and should have a valid user ID.
   * @return The updated {@code User} object after successful persistence to the database.
   * @throws FailedOperationException If an error occurs during the update operation,
   *                                   such as a database failure.
   */
  private User persistUpdate(User user) throws FailedOperationException {
    try {
      return userDAO.update(user);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to update user: " + user.getUserId(), e);
      throw new FailedOperationException("Failed to update user account", e);
    }
  }

  private void syncAuthorNameIfLinked(User user) throws FailedOperationException {
    if (user.getUserId() == null || user.getRole() != UserRole.AUTHOR) return;

    AuthorService authorService =
        (AuthorService) ApplicationContextProvider.getServletContext().getAttribute("authorService");

    if (authorService == null) {
      throw new FailedOperationException("AuthorService not found.");
    }

    authorService.syncWithUser(user);
  }
}
