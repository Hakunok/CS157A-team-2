package com.airchive.util;

import com.airchive.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input and entity field values before persistence.
 *
 * <p>This class centralizes all application-specific validation logic for fields like email
 * addresses, usernames, names, passwords, topic codes and names, and publication or collection
 * titles.
 *
 * <p>Each {@code validateX()} method throws a {@link ValidationException} on failure, while
 * {@code isValidX()} methods return a boolean.
 *
 * <p>These methods are intended to be used in the service layer before calling repositories.
 */
public class ValidationUtils {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

  private static final Pattern USERNAME_PATTERN =
      Pattern.compile("^[a-z0-9](?!.*[._-]{2})[a-z0-9._-]{1,18}[a-z0-9]$");

  private static final Pattern NAME_PATTERN =
      Pattern.compile("^[A-Za-z'\\- ]+$");

  private static final Pattern TOPIC_CODE_PATTERN =
      Pattern.compile("^[a-z0-9-]+$");

  private ValidationUtils() {}

  /**
   * Validates that the given email is syntactically valid and within allowed length.
   *
   * @param email the email address to validate
   * @throws ValidationException if the email is null, malformed, or violates length constraints
   */
  public static void validateEmail(String email) {
    if (!isValidEmail(email)) {
      throw new ValidationException("Invalid email address.");
    }
  }

  /**
   * Validates that the given email is syntactically valid and within allowed length.
   *
   * @param email the email address to validate
   * @throws ValidationException if the email is null, malformed, or violates length constraints
   */
  public static boolean isValidEmail(String email) {
    return email != null
        && email.trim().length() >= 3
        && email.trim().length() <= 75
        && EMAIL_PATTERN.matcher(email.trim()).matches();
  }

  /**
   * Validates that the username meets character and length constraints.
   *
   * <p>Usernames must be 3–20 characters, lowercase, and may contain letters, digits,
   * dashes, underscores, or periods. Cannot start or end with a special character.
   *
   * @param username the username to validate
   * @throws ValidationException if the username is invalid
   */
  public static void validateUsername(String username) {
    if (!isValidUsername(username)) {
      throw new ValidationException("Invalid username.");
    }
  }

  /**
   * Checks whether the given username is valid.
   *
   * @param username the username to check
   * @return true if the username meets format and length rules
   */
  public static boolean isValidUsername(String username) {
    return username != null
        && username.length() >= 3
        && username.length() <= 20
        && USERNAME_PATTERN.matcher(username.trim().toLowerCase()).matches();
  }

  /**
   * Validates that a name (first or last) contains only allowed characters.
   *
   * <p>Names may contain alphabetic characters, spaces, apostrophes, or dashes,
   * and must not exceed 40 characters.
   *
   * @param name the name to validate
   * @throws ValidationException if the name is empty, too long, or contains invalid characters
   */
  public static void validateName(String name) {
    if (!isValidName(name)) {
      throw new ValidationException("Name must be 1–40 characters and alphabetic.");
    }
  }

  /**
   * Checks whether the given name is valid.
   *
   * @param name the name to check
   * @return true if the name is non-empty, short enough, and matches the pattern
   */
  public static boolean isValidName(String name) {
    return name != null
        && !name.trim().isEmpty()
        && name.trim().length() <= 40
        && NAME_PATTERN.matcher(name.trim()).matches();
  }

  /**
   * Validates that a password meets minimum security requirements.
   *
   * <p>Passwords must be at least 8 characters long.
   *
   * @param password the password to validate
   * @throws ValidationException if the password is too short or null
   */
  public static void validatePassword(String password) {
    if (!isValidPassword(password)) {
      throw new ValidationException("Password must be at least 8 characters.");
    }
  }

  /**
   * Checks whether the password meets minimum length requirements.
   *
   * @param password the password to check
   * @return true if the password is valid; false otherwise
   */
  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  /**
   * Validates the format of a topic code (e.g., {@code ai-safety}, {@code nlp}).
   *
   * <p>Codes must be lowercase, 1–10 characters long, and contain only letters,
   * numbers, or hyphens.
   *
   * @param code the topic code to validate
   * @throws ValidationException if the code is invalid
   */
  public static void validateTopicCode(String code) {
    if (!isValidTopicCode(code)) {
      throw new ValidationException("Topic code must be 1–10 alphanumeric characters.");
    }
  }

  /**
   * Checks whether a topic code is valid.
   *
   * @param code the topic code to check
   * @return true if the code matches the required format
   */
  public static boolean isValidTopicCode(String code) {
    return code != null
        && !code.trim().isEmpty()
        && code.length() <= 10
        && TOPIC_CODE_PATTERN.matcher(code).matches();
  }

  /**
   * Validates that a topic's full display name is non-empty and within character limits.
   *
   * @param fullName the full name of the topic
   * @throws ValidationException if the name is empty or exceeds 50 characters
   */
  public static void validateTopicFullName(String fullName) {
    if (!isValidTopicFullName(fullName)) {
      throw new ValidationException("Topic name must be 1–50 characters.");
    }
  }

  /**
   * Checks whether a topic full name is valid.
   *
   * @param fullName the topic full name to check
   * @return true if the name is valid
   */
  public static boolean isValidTopicFullName(String fullName) {
    return fullName != null
        && !fullName.trim().isEmpty()
        && fullName.length() <= 50;
  }

  /**
   * Validates that a title is within the maximum allowed length.
   *
   * @param title the title to validate
   * @throws ValidationException if the title is too long or null
   */
  public static void validateTitle(String title) {
    if (!isValidTitle(title)) {
      throw new ValidationException("Title can be up to 150 characters long.");
    }
  }

  /**
   * Checks whether a title is valid based on length constraints.
   *
   * @param title the title to check
   * @return true if the title is valid
   */
  public static boolean isValidTitle(String title) {
    return title != null && title.length() <= 150;
  }
}