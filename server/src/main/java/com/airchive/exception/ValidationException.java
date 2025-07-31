package com.airchive.exception;

/**
 * Thrown when a user-provided input or entity field has failed validation.
 *
 * <p>Mapped to a {@code 400 Bad Request} response by JAX-RS.
 */
public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}