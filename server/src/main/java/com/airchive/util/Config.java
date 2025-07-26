package com.airchive.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading and retrieving configuration properties
 * from a predefined properties file located in the classpath.
 */
public class Config {
  private static final Properties properties = new Properties();

  static {
    try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("db.properties");) {
      if (inputStream == null) {
        throw new IOException("db.properties file not found");
      }

      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load application properties.", e);
    }
  }

  public static String getProperty(String key) {
    return properties.getProperty(key);
  }
}
