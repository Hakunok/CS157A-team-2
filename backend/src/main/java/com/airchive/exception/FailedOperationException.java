package com.airchive.exception;

public class FailedOperationException extends Exception {
  public FailedOperationException(String message) {
    super(message);
  }

  public FailedOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
