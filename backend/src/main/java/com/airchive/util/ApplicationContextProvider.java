package com.airchive.util;

import javax.servlet.ServletContext;

public class ApplicationContextProvider {
  private static ServletContext context;

  public static void setServletContext(ServletContext servletContext) {
    ApplicationContextProvider.context = servletContext;
  }

  public static ServletContext getServletContext() {
    return context;
  }
}
