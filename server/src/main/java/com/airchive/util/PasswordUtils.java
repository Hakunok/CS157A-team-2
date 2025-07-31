package com.airchive.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for securely hashing and verifying passwords using {@link BCrypt}.
 *
 * <p>This class is designed to provide password management functionality for login and account
 * creation.
 */
public class PasswordUtils {

  /** BCrypt cost factor for password hashing. */
  private static final int BCRYPT_COST = 12;

  /** Private constructor to prevent instantiation. */
  private PasswordUtils() {}

  /**
   * Hashes a plain text password using the BCrypt algorithm.
   * The returned hash includes the salt and can be stored in the database.
   *
   * @param plainPassword the raw password entered by the user
   * @return a BCrypt hash string (with salt) that can be stored for future verification
   */
  public static String hashPassword(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
  }

  /**
   * Verifies a plain text password against a previously hashed BCrypt password.
   *
   * @param plainPassword the raw password input to verify
   * @param hashedPassword the stored BCrypt hash to compare against
   * @return {@code true} if the password matches; {@code false} otherwise
   */
  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}