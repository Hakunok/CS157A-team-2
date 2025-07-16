package com.airchive.exception;

/**
 * Represents an exception that is thrown when an operation fails to execute successfully.
 * The messages from this exception will be used to display on the UI.
 */
public class FailedOperationException extends Exception {
  public FailedOperationException(String message) {
    super(message);
  }

  public FailedOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
