package com.airchive.util;


import java.util.regex.Pattern;

/**
 * Utility class providing static methods for validating email and username inputs.
 */
public class ValidationUtils {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

  private static final Pattern USERNAME_ALLOWED_CHARS_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

  private ValidationUtils() {}

  public static boolean isValidEmail(String email) {
    if (email == null) return false;
    String trimmed = email.trim();
    return trimmed.length() >= 3 && trimmed.length() <= 75 && EMAIL_PATTERN.matcher(trimmed).matches();
  }

  public static boolean isValidUsername(String username) {
    if (username == null) return false;
    String trimmed = username.trim();
    return trimmed.length() >= 3 && trimmed.length() <= 20
        && !trimmed.startsWith("_") && !trimmed.endsWith("_")
        && USERNAME_ALLOWED_CHARS_PATTERN.matcher(trimmed).matches();
  }

  public static boolean isValidName(String name) {
    if (name == null) return false;
    String trimmed = name.trim();
    return !trimmed.isEmpty() && trimmed.length() <= 40;
  }

  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }
}
