package com.airchive.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password-related operations such as hashing and verification.
 * This class provides static methods that leverage BCrypt for secure password management.
 */
public class PasswordUtil {

  public static String hashPassword(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
  }

  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}
