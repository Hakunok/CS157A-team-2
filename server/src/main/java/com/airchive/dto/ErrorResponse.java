package com.airchive.dto;

import java.util.Map;

/**
 * This record represents the structure for returning error information in REST API responses.
 *
 * <p>This response is intended to be returned with HTTP status codes by {@link javax.ws.rs.ext.ExceptionMapper}.
 *
 * @param message
 * @param fieldErrors
 */
public record ErrorResponse(
    String message,
    Map<String, String> fieldErrors
) {

  /**
   * Creates an {@code ErrorResponse} with a message and no field-level errors.
   *
   * @param message the global error message
   * @return an {@code ErrorResponse} with {@code fieldErrors = null}
   */
  public static ErrorResponse of(String message) {
    return new ErrorResponse(message, null);
  }

  /**
   * Creates an {@code ErrorResponse} with a message and detailed field errors.
   *
   * @param message the global error message
   * @param fieldErrors a map of specific field error messages
   * @return an {@code ErrorResponse} with both global and field-level errors
   */
  public static ErrorResponse of(String message, Map<String, String> fieldErrors) {
    return new ErrorResponse(message, fieldErrors);
  }
}