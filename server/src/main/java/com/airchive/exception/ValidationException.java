package com.airchive.exception;

public class ValidationException extends Exception{
  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
