package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link EntityNotFoundException} to a {@code 404 Not Found} HTTP response.
 */
@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
  @Override
  public Response toResponse(EntityNotFoundException ex) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}