package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
  @Override
  public Response toResponse(AuthenticationException ex) {
    return Response.status(Response.Status.UNAUTHORIZED)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}
