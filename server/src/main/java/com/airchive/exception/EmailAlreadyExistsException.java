package com.airchive.exception;

public class EmailAlreadyExistsException extends UserAlreadyExistsException {
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
