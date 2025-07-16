package com.airchive.util;


import java.util.regex.Pattern;

/**
 * Utility class for validating input data such as email, username, name, and password.
 * This class contains static methods that are used to perform validations
 * based on predefined patterns or rules for different input types.
 *
 * The utility is designed to prevent instantiation by having a private constructor.
 */
public class ValidationUtils {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

  private static final Pattern USERNAME_PATTERN =
      Pattern.compile("^[a-z0-9](?!.*[._-]{2})[a-z0-9._-]{1,18}[a-z0-9]$");

  private ValidationUtils() {}

  public static boolean isValidEmail(String email) {
    if (email == null) return false;
    String trimmed = email.trim();
    return trimmed.length() >= 3 && trimmed.length() <= 75 && EMAIL_PATTERN.matcher(trimmed).matches();
  }

  public static boolean isValidUsername(String username) {
    if (username == null) return false;
    String trimmed = username.trim().toLowerCase();
    return trimmed.length() >= 3 && trimmed.length() <= 20
        && USERNAME_PATTERN.matcher(trimmed).matches();
  }

  public static boolean isValidName(String name) {
    if (name == null) return false;
    String trimmed = name.trim();
    return !trimmed.isEmpty()
        && trimmed.length() <= 40
        && trimmed.matches("^[A-Za-z'\\- ]+$");
  }

  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }
}
