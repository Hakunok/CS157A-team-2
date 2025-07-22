package com.airchive.exception;

/**
 * Indicates a business error during a database operation, as opposed to a system error.
 */
public class PersistenceException extends RuntimeException {

  public PersistenceException(String message) {
    super(message);
  }

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
