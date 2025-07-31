package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link ValidationException} to a {@code 400 Bad Request} HTTP response.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
  @Override
  public Response toResponse(ValidationException ex) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}