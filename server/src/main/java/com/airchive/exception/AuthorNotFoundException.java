package com.airchive.exception;

public class AuthorNotFoundException extends UserNotFoundException {
  public AuthorNotFoundException(String message) {
    super(message);
  }
}
