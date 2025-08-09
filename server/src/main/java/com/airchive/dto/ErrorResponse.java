package com.airchive.dto;

import java.util.Map;

/**
 * Response body for conveying error information to the client.
 * <p>
 * This DTO is used by our {@link javax.ws.rs.ext.ExceptionMapper} implementations to return structured
 * error messages to clients along with the appropriate HTTP status code.
 *
 * @param message the error message
 * @param fieldErrors an optional map of field names and their error messages
 */
public record ErrorResponse(
    String message,
    Map<String, String> fieldErrors
) {

  /**
   * Creates an {@code ErrorResponse} with a message and no field-level errors.
   *
   * @param message the error message
   * @return an {@code ErrorResponse} with {@code fieldErrors = null}
   */
  public static ErrorResponse of(String message) {
    return new ErrorResponse(message, null);
  }

  /**
   * Creates an {@code ErrorResponse} with a message and field errors.
   *
   * @param message the global error message
   * @param fieldErrors a map of field names to error messages
   * @return an {@code ErrorResponse} both global and field-level error information
   */
  public static ErrorResponse of(String message, Map<String, String> fieldErrors) {
    return new ErrorResponse(message, fieldErrors);
  }
}