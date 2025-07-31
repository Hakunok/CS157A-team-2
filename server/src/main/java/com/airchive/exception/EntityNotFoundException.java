package com.airchive.exception;

/**
 * Thrown when an entity is not found by a repository.
 *
 * <p>Mapped to a {@code 404 Not Found} response by JAX-RS.
 */
public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException(String message) {
    super(message);
  }
}
