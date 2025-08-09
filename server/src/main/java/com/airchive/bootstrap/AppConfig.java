package com.airchive.bootstrap;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

/**
 * This class configures the Jersey REST API for our application.
 * <p>
 * This class extends {@link ResourceConfig} and is automatically loaded by the servlet container (Tomcat),
 * at application startup. It configures the base API path and the packages Jersey should scan REST
 * resources and providers for.
 * <p>
 * The {@link ApplicationPath} annotation sets the root URL for all REST endpoints to {@code /api}. As a result,
 * all our resources annotated with, {@code @Path("/path/to/resource}, will be available at
 * {@code /api/path/to/resource}.
 * <p>
 * Inside the constructor, the {@code packages(...)} method registers packages containing components necessary
 * for our REST API:
 * <ul>
 *   <li>{@code com.airchive.resource}: this package contains classes defining our REST API</li>
 *   <li>{@code com.airchive.exception}: this package contains our custom {@link Exception}s and
 *   {@link javax.ws.rs.ext.ExceptionMapper}s which map our exceptions to
 *   the appropriate HTTP status codes for our client to use.</li>
 * </ul>
 */
@ApplicationPath("/api")
public class AppConfig extends ResourceConfig {
  public AppConfig() {
    packages(
        "com.airchive.resource",
        "com.airchive.exception"
    );
  }
}