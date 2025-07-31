package com.airchive.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading and retrieving configuration properties from a {@code xyz
 * .properties} file located in the classpath.
 *
 * <p>Example {@code db.properties}:</p>
 * <pre>{@code
 * db.user=root
 * db.password=secret
 * db.dbName=airchive
 * db.usePool=true
 * }
 * </pre>
 */
public class PropertyUtils {

  private static final Properties properties = new Properties();

  static {
    try (InputStream inputStream = PropertyUtils.class.getClassLoader().getResourceAsStream("db.properties");) {
      if (inputStream == null) {
        throw new IOException("db.properties file not found");
      }

      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load application properties.", e);
    }
  }

  /**
   * Retrieves a property value by key.
   * @param key the name of the property
   * @return the property value, or {@code null} if not defined
   */
  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Retrieves the boolean property value by key with a default fallback.
   * @param key the name of the property
   * @param defaultValue the default value to return if the property is not present
   * @return the parsed boolean value of the property, or {@code defaultValue} if not set
   */
  public static boolean getBooleanProperty(String key, boolean defaultValue) {
    String value = properties.getProperty(key);
    return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
  }
}
