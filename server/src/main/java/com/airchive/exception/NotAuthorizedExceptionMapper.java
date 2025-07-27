package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {
  @Override
  public Response toResponse(NotAuthorizedException ex) {
    return Response.status(Response.Status.UNAUTHORIZED)
        .entity(ErrorResponse.of("Authentication required."))
        .build();
  }
}
