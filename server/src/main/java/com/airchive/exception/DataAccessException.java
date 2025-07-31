package com.airchive.exception;

/**
 * Thrown whenever a database error occurs (e.g. {@link java.sql.SQLException}).
 *
 * <p>Mapped to a {@code 500 Internal Server Error} response by JAX-RS.
 */
public class DataAccessException extends RuntimeException {
  public DataAccessException(String message) {
    super(message);
  }

  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}