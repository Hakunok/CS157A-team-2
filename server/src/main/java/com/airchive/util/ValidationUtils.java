package com.airchive.util;

import com.airchive.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Utility class for validating entity attributes. All fields should be validated and sanitized
 * by service classes before they are persisted in the database.
 * Criteria for validity is derived from application logic and data schema.
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

  public static void validateEmail(String email) {
    if (!isValidEmail(email)) {
      throw new ValidationException("Invalid email address.");
    }
  }

  public static boolean isValidEmail(String email) {
    return email != null
        && email.trim().length() >= 3
        && email.trim().length() <= 75
        && EMAIL_PATTERN.matcher(email.trim()).matches();
  }

  public static void validateUsername(String username) {
    if (!isValidUsername(username)) {
      throw new ValidationException("Invalid username.");
    }
  }

  public static boolean isValidUsername(String username) {
    return username != null
        && username.length() >= 3
        && username.length() <= 20
        && USERNAME_PATTERN.matcher(username.trim().toLowerCase()).matches();
  }

  public static void validateName(String name) {
    if (!isValidName(name)) {
      throw new ValidationException("Name must be 1–40 characters and alphabetic.");
    }
  }

  public static boolean isValidName(String name) {
    return name != null
        && !name.trim().isEmpty()
        && name.trim().length() <= 40
        && NAME_PATTERN.matcher(name.trim()).matches();
  }

  public static void validatePassword(String password) {
    if (!isValidPassword(password)) {
      throw new ValidationException("Password must be at least 8 characters.");
    }
  }

  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  public static void validateTopicCode(String code) {
    if (!isValidTopicCode(code)) {
      throw new ValidationException("Topic code must be 1–10 alphanumeric characters.");
    }
  }

  public static boolean isValidTopicCode(String code) {
    return code != null
        && !code.trim().isEmpty()
        && code.length() <= 10
        && TOPIC_CODE_PATTERN.matcher(code).matches();
  }

  public static void validateTopicFullName(String fullName) {
    if (!isValidTopicFullName(fullName)) {
      throw new ValidationException("Topic name must be 1–50 characters.");
    }
  }

  public static boolean isValidTopicFullName(String fullName) {
    return fullName != null
        && !fullName.trim().isEmpty()
        && fullName.length() <= 50;
  }
}
