package com.airchive.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing static methods for validating email and username inputs.
 */
public class ValidationUtils {
  private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  private static final String USERNAME_REGEX = "^[a-zA-Z0-9]([a-zA-Z0-9_]*[a-zA-Z0-9])?$";

  public static boolean isValidEmail(String email) {
    if (email == null && email.isEmpty()) {
      return false;
    }

    Matcher matcher = EMAIL_PATTERN.matcher(email);
    return matcher.matches();
  }

  public static boolean isValidUsernameFormat(String username) {
    return username != null && username.matches(USERNAME_REGEX);
  }

  public static boolean isValidUsernameLength(String username) {
    return username != null && username.length() >= 3 && username.length() <= 20;
  }
}
