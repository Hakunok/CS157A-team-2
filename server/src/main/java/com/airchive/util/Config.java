package com.airchive.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


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
