package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link ForbiddenException} to a {@code 403 Forbidden} HTTP response.
 */
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
  @Override
  public Response toResponse(ForbiddenException ex) {
    return Response.status(Response.Status.FORBIDDEN)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}