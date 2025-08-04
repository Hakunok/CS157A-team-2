package com.airchive.exception;

import com.airchive.dto.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link Exception} to a {@code 500 Internal Server Error} HTTP response.
 */
@Provider
public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {
  @Override
  public Response toResponse(DataAccessException ex) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(ErrorResponse.of(ex.getMessage()))
        .build();
  }
}