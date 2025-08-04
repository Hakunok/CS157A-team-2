package com.airchive.bootstrap;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api") // sets the base path for all REST endpoints
public class AppConfig extends ResourceConfig {
  public AppConfig() {
    // Automatically scan these packages for @Path, @Provider, filters, mappers, etc.
    packages(
        "com.airchive.resource",    // This will pick up GlobalCorsResource
        "com.airchive.bootstrap",   // This will pick up CorsFilter
        "com.airchive.exception"
    );
  }
}