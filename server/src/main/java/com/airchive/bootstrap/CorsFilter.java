package com.airchive.bootstrap;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * This filter intercepts all requests to add CORS headers to the response,
 * allowing the React frontend (on a different origin) to communicate with the API.
 * @TODO: we will remove this once we build our react application
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    headers.add("Access-Control-Allow-Origin", "http://localhost:5173");
    headers.add("Access-Control-Allow-Credentials", "true");
    headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}