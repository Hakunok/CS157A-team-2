package com.airchive.util;

import javax.ws.rs.core.Response;

/**
 * Utility class for creating HTTP {@link Response} objects
 * with predefined structures and commonly used status codes.
 * Responses can be constructed using entities or messages.
 */
public class JsonUtil {

  public static Response ok(Object entity) {
    return Response.ok(entity).build();
  }

  public static Response ok(String message) {
    return Response.ok(new MessageResponse(message)).build();
  }

  public static Response created(Object entity) {
    return Response.status(Response.Status.CREATED).entity(entity).build();
  }

  public static Response badRequest(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorResponse("BAD_REQUEST", message))
        .build();
  }

  public static Response unauthorized(String message) {
    return Response.status(Response.Status.UNAUTHORIZED)
        .entity(new ErrorResponse("UNAUTHORIZED", message))
        .build();
  }

  public static Response forbidden(String message) {
    return Response.status(Response.Status.FORBIDDEN)
        .entity(new ErrorResponse("FORBIDDEN", message))
        .build();
  }

  public static Response notFound(String message) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("NOT_FOUND", message))
        .build();
  }

  public static Response conflict(String message) {
    return Response.status(Response.Status.CONFLICT)
        .entity(new ErrorResponse("CONFLICT", message))
        .build();
  }

  public static Response internalError(String message) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("INTERNAL_ERROR", message))
        .build();
  }

  /**
   * Represents a response structure containing a single message.
   * @param message the message content; must not be null
   */
  public record MessageResponse(String message) {}

  /**
   * Represents an error response structure with an error code or type and an associated message.
   * @param error a code or identifier representing the type of error
   * @param message the message content; must not be null
   */
  public record ErrorResponse(String error, String message) {}
}
