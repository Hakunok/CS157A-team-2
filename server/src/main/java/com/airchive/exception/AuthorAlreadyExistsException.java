package com.airchive.exception;

public class AuthorAlreadyExistsException extends UserAlreadyExistsException {
  public AuthorAlreadyExistsException(String message) {
    super(message);
  }
}
