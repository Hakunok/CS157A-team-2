package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.*;
import com.airchive.entity.Account;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.UserRepository;
import com.airchive.util.PasswordUtil;
import com.airchive.util.ValidationUtil;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Account signup(SignupRequest request) throws ValidationException {
    ValidationResponse validation = validateUserCreation(request);
    if (!validation.valid()) {
      throw new ValidationException("Validation failed");
    }

    Optional<Account> existing = userRepository.findByEmail(request.email().trim().toLowerCase());
    if (existing.isPresent() && existing.get().isPlaceholder()) {
      return claimExistingAuthor(existing.get().userId(), request);
    }

    Account user = new Account(
        0,
        request.username().trim(),
        request.firstName().trim(),
        request.lastName().trim(),
        request.email().trim().toLowerCase(),
        PasswordUtil.hashPassword(request.password()),
        Account.Permission.READER,
        false,
        false,
        null
    );

    return userRepository.create(user);
  }

  public Account createPlaceholderAuthor(String firstName, String lastName, String email) {
    Account placeholder = new Account(
        0,
        null,
        firstName.trim(),
        lastName.trim(),
        email.trim().toLowerCase(),
        null,
        Account.Permission.AUTHOR,
        false,
        true,
        null
    );
    return userRepository.create(placeholder);
  }

  public Account claimExistingAuthor(int userId, SignupRequest request) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      userRepository.updateUsername(userId, request.username().trim(), conn);
      userRepository.updatePasswordHash(userId, PasswordUtil.hashPassword(request.password()), conn);
      userRepository.updateIsPlaceholder(userId, false, conn);

      Account updated = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found after claim."));

      tx.commit();
      return updated;
    }
  }

  public Account signin(SigninRequest request) throws AuthenticationException {
    Account user = userRepository.findByUsernameOrEmail(request.usernameOrEmail().trim())
        .orElseThrow(() -> new AuthenticationException("Incorrect login credentials."));

    if (!PasswordUtil.verifyPassword(request.password(), user.passwordHash())) {
      throw new AuthenticationException("Incorrect login credentials.");
    }

    return user;
  }

  public Account adminUpdateUser(int userId, AdminUpdateUserRequest request)
      throws EntityNotFoundException, ValidationException {

    if (request.permission() == null) {
      throw new ValidationException("Permission must be specified.");
    }

    try {
      Account.Permission.valueOf(request.permission().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid permission.");
    }

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Account user = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found."));

      if (!request.permission().equalsIgnoreCase(user.permission().name())) {
        userRepository.updatePermission(userId, Account.Permission.valueOf(request.permission()), conn);
      }

      if (request.isAdmin() != null && request.isAdmin() != user.isAdmin()) {
        userRepository.updateIsAdmin(userId, request.isAdmin(), conn);
      }

      Account updated = userRepository.findById(userId, conn)
          .orElseThrow(() -> new EntityNotFoundException("User not found after update."));

      tx.commit();
      return updated;
    }
  }

  public List<Account> getAllAuthors() {
    return userRepository.findAllAuthors();
  }

  public List<Account> getAllPlaceholderAuthors() {
    return userRepository.findAllPlaceholderAuthors();
  }

  public void approveAuthorRequest(int userId) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      userRepository.updatePermission(userId, Account.Permission.AUTHOR, conn);
      userRepository.updateIsPlaceholder(userId, false, conn);

      tx.commit();
    }
  }

  public Account getUserById(int userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("This user does not exist."));
  }

  public ValidationResponse validateUsername(String username) {
    if (!ValidationUtil.isValidUsernameForUser(username.trim())) {
      return ValidationResponse.failure("Username must be 3–20 characters and may include letters, numbers, ., _, - (not at the start/end or repeated).");
    }
    if (userRepository.existsByUsername(username.trim())) {
      return ValidationResponse.failure("This username is already in use.");
    }
    return ValidationResponse.success();
  }

  public ValidationResponse validateEmail(String email) {
    if (!ValidationUtil.isValidEmailForUser(email.trim())) {
      return ValidationResponse.failure("Please enter a valid email address.");
    }
    if (userRepository.existsByEmail(email.trim().toLowerCase())) {
      return ValidationResponse.failure("This email is already in use.");
    }
    return ValidationResponse.success();
  }

  public ValidationResponse validatePassword(String password) {
    return ValidationUtil.isValidPasswordForUser(password)
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

    return allValid
        ? ValidationResponse.success()
        : ValidationResponse.failure("One or more fields are invalid. Please check your input.");
  }
}