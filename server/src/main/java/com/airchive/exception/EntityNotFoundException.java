package com.airchive.exception;

public class EntityNotFoundException extends DataAccessException {

  public EntityNotFoundException(String message) {
    super(message, null);
  }
}
