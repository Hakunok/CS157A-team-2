package com.airchive.util;


import com.airchive.entity.Account;
import java.util.regex.Pattern;

/**
 * Utility class for validating entity attributes. All fields should be validated and sanitized
 * by service classes before they are persisted in the database.
 * Criteria for validity is derived from application logic and data schema.
 */
public class ValidationUtil {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

  private static final Pattern USERNAME_PATTERN =
      Pattern.compile("^[a-z0-9](?!.*[._-]{2})[a-z0-9._-]{1,18}[a-z0-9]$");

  private ValidationUtil() {}

  /**
   * Checks if a string is a valid email address for a {@link Account}.
   * Email must be 3-75 characters and match the email regex pattern.
   */
  public static boolean isValidEmailForUser(String email) {
    if (email == null) return false;
    String trimmed = email.trim();
    return trimmed.length() >= 3 && trimmed.length() <= 75 && EMAIL_PATTERN.matcher(trimmed).matches();
  }

  /**
   * Checks if a string is a valid username for a {@link Account}.
   * Username must be 3-20 characters, start/end with alphanumeric, and cannot have consecutive
   * special characters.
   */
  public static boolean isValidUsernameForUser(String username) {
    if (username == null) return false;
    String trimmed = username.trim().toLowerCase();
    return trimmed.length() >= 3 && trimmed.length() <= 20
        && USERNAME_PATTERN.matcher(trimmed).matches();
  }

  /**
   * Checks if a string is a valid first/last name for a {@link Account} or
   * {@link com.airchive.entity.Author}.
   * A valid name must be 1-40 characters and only contain alphabetic characters.
   */
  public static boolean isValidName(String name) {
    if (name == null) return false;
    String trimmed = name.trim();
    return !trimmed.isEmpty()
        && trimmed.length() <= 40
        && trimmed.matches("^[A-Za-z'\\- ]+$");
  }

  /**
   * Checks if a string is a valid password for a {@link Account}.
   * A password must contain 8 or more characters.
   */
  public static boolean isValidPasswordForUser(String password) {
    return password != null && password.length() >= 8;
  }
}
