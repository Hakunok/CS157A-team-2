package com.airchive.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
  private static Properties properties = new Properties();
  private static final String PROPERTIES_FILE_NAME = "db.properties";

  static {
    try (InputStream inputStream =
        PropertyLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
      if (inputStream == null) {
        throw new IOException("Properties file not found: " + PROPERTIES_FILE_NAME);
      }

      properties.load(inputStream);
      System.out.println(PROPERTIES_FILE_NAME + " loaded successfully");
    } catch (IOException e) {
      System.err.println(PROPERTIES_FILE_NAME + " could not be loaded");
      e.printStackTrace();
      throw new RuntimeException("Failed to load application properties.", e);
    }
  }

  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  public static String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  public static Properties getProperties() {
    return new Properties(properties);
  }
}
