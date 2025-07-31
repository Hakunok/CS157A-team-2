package com.airchive.exception;

/**
 * Thrown when a user attempts an action without proper authentication.
 *
 * <p>Mapped to a {@code 401 Unauthorized} response.
 */
public class AuthenticationException extends RuntimeException {
  public AuthenticationException(String message) {
    super(message);
  }
}
