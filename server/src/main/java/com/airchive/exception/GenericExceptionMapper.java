package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link Exception} to a {@code 500 Internal Server Error} HTTP response.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
  @Override
  public Response toResponse(Exception ex) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(ErrorResponse.of("An unexpected error occurred."))
        .build();
  }
}