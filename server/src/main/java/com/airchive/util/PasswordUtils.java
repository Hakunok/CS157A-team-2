package com.airchive.util;

import org.mindrot.jbcrypt.BCrypt;

/** Provides static utility methods for secure password hashing and verification using BCrypt. */
public class PasswordUtils {

  /** BCrypt cost factor for password hashing. */
  private static final int BCRYPT_COST = 12;

  private PasswordUtils() {}

  /**
   * Hashes a plain text password using Bcrypt with a cost factor of 12.
   * Each call generates a unique salt, so the same password will produce different hashes.
   */
  public static String hashPassword(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
  }

  /**
   * Verifies a plain text password against a BCrypt hashed password.
   */
  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}
