package com.airchive.exception;

import com.airchive.dto.ErrorResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps {@link BadRequestException} to a {@code 400 Bad Request} HTTP response.
 */
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
  @Override
  public Response toResponse(BadRequestException ex) {
    return Response.status(Status.BAD_REQUEST)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}