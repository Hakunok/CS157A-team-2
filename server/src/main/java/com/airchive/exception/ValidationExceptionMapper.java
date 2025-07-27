package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
  @Override
  public Response toResponse(ValidationException ex) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}