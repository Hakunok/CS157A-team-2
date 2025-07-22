package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.AdminUpdateUserRequest;
import com.airchive.dto.SignupRequest;
import com.airchive.dto.UpdateUserRequest;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.entity.User;
import com.airchive.repository.UserRepository;
import com.airchive.dto.SigninRequest;
import com.airchive.dto.UserResponse;
import com.airchive.dto.ValidationResponse;
import com.airchive.util.PasswordUtil;
import com.airchive.util.ValidationUtil;
import com.airchive.exception.AuthenticationException;
import java.sql.Connection;


public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse signup(SignupRequest request) throws ValidationException {

    ValidationResponse validation = validateUserCreation(request);
    if (!validation.valid()) {
      throw new ValidationException("Validation failed");
    }

    User toCreate = new User(
        request.username().trim(),
        request.firstName().trim(),
        request.lastName().trim(),
        request.email().trim().toLowerCase(),
        PasswordUtil.hashPassword(request.password())
    );

    User newUser = userRepository.create(toCreate);

    return UserResponse.fromUser(newUser);
  }

  public UserResponse signin(SigninRequest request) throws AuthenticationException {
    User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail().trim())
        .orElseThrow(() -> new AuthenticationException("Incorrect login credentials."));

    if (!PasswordUtil.verifyPassword(request.password(), user.passwordHash())) {
      throw new AuthenticationException("Incorrect login credentials.");
    }

    return UserResponse.fromUser(user);
  }

  public UserResponse updateUser(int userId, UpdateUserRequest request) throws EntityNotFoundException, ValidationException {
    boolean isUpdateValid =
        (request.username() == null || validateUsername(request.username()).valid()) &&
            (request.firstName() == null || validateFirstName(request.firstName()).valid()) &&
            (request.lastName() == null || validateLastName(request.lastName()).valid()) &&
            (request.password() == null || validatePassword(request.password()).valid());

    if (!isUpdateValid) {
      throw new ValidationException("Validation failed");
    }

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      User user = userRepository.findById(userId, conn).orElseThrow(() -> new EntityNotFoundException("User not found."));

      if (request.username() != null && !request.username().equals(user.username())) {
        userRepository.updateUsername(userId, request.username(), conn);
      }

      if (request.firstName() != null && !request.firstName().equals(user.firstName())) {
        userRepository.updateFirstName(userId, request.firstName(), conn);
      }

      if (request.lastName() != null && !request.lastName().equals(user.lastName())) {
        userRepository.updateLastName(userId, request.lastName(), conn);
      }

      if (request.password() != null) {
        String newHash = PasswordUtil.hashPassword(request.password());
        if (!newHash.equals(user.passwordHash())) {
          userRepository.updatePasswordHash(userId, newHash, conn);
        }
      }

      User updated =
          userRepository.findById(userId, conn).orElseThrow(() -> new EntityNotFoundException("User not found."));

      tx.commit();
      return UserResponse.fromUser(updated);
    }
  }

  public UserResponse adminUpdateUser(int userId, AdminUpdateUserRequest request) throws EntityNotFoundException, ValidationException {
    if (request.permission() == null && request.status() == null) {
      throw new ValidationException("At least one field must be specified.");
    }

    if (request.permission() != null) {
      try {
        User.Permission.valueOf(request.permission().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new ValidationException("Invalid permission.");
      }
    }

    if (request.status() != null) {
      try {
        User.Status.valueOf(request.status().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new ValidationException("Invalid status.");
      }
    }

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      User user = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found."));

      if (request.permission() != null && !request.permission().equalsIgnoreCase(user.permission().name())) {
        userRepository.updatePermission(userId, User.Permission.valueOf(request.permission()), conn);
      }

      if (request.status() != null && !request.status().equalsIgnoreCase(user.status().name())) {
        userRepository.updateStatus(userId, User.Status.valueOf(request.status()), conn);
      }

      User updated = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found after update."));

      tx.commit();
      return UserResponse.fromUser(updated);
    }
  }

  public UserResponse getUserById(int userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("This user does not exist."));

    return UserResponse.fromUser(user);
  }

  public ValidationResponse validateUsername(String username) {
    if (!ValidationUtil.isValidUsername(username.trim())) {
      return ValidationResponse.failure("Username can use 3–20 letters, numbers, or . _ - (no symbols at the edges or back-to-back).");
    }

    if (userRepository.existsByUsername(username.trim())) {
      return ValidationResponse.failure("This username is already in use.");
    }

    return ValidationResponse.success();
  }

  public ValidationResponse validateEmail(String email) {
    if (!ValidationUtil.isValidEmail(email.trim())) {
      return ValidationResponse.failure("Please enter a valid email address.");
    }

    if (userRepository.existsByEmail(email.trim().toLowerCase())) {
      return ValidationResponse.failure("This email is already in use.");
    }

    return ValidationResponse.success();
  }

  public ValidationResponse validatePassword(String password) {
    return ValidationUtil.isValidPassword(password)
        ? ValidationResponse.success()
        : ValidationResponse.failure("Password must be at least 8 characters long.");
  }

  public ValidationResponse validateFirstName(String firstName) {
    return ValidationUtil.isValidName(firstName)
        ? ValidationResponse.success()
        : ValidationResponse.failure("First name contains invalid characters or is too long.");
  }

  public ValidationResponse validateLastName(String lastName) {
    return ValidationUtil.isValidName(lastName)
        ? ValidationResponse.success()
        : ValidationResponse.failure("Last name contains invalid characters or is too long.");
  }

  private ValidationResponse validateUserCreation(SignupRequest request) {
    boolean allValid = validateUsername(request.username()).valid() &&
        validateEmail(request.email()).valid() &&
        validatePassword(request.password()).valid() &&
        validateFirstName(request.firstName()).valid() &&
        validateLastName(request.lastName()).valid();

    if (!allValid) {
      return ValidationResponse.failure("One or more fields are invalid. Please check your input.");
    }

    return ValidationResponse.success();
  }
}
