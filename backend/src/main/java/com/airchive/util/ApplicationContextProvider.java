package com.airchive.util;

import javax.servlet.ServletContext;

/**
 * A utility class for providing access to the {@link ServletContext}.
 * This class allows setting and retrieving the {@link ServletContext} instance for use across the application.
 * It provides a static context that can be accessed globally within the application.
 */
public class ApplicationContextProvider {
  private static ServletContext context;

  public static void setServletContext(ServletContext servletContext) {
    ApplicationContextProvider.context = servletContext;
  }

  public static ServletContext getServletContext() {
    return context;
  }
}
