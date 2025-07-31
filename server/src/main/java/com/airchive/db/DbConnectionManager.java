package com.airchive.db;

import com.airchive.util.PropertyUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The {@code DbConnectionManager} class provides a centralized mechanism for managing database
 * connections in the application. It supports both pooled and non-pooled connections based on
 * the configurations provided via {@code db.properties}.
 * Connection pooling using {@link HikariDataSource} is enabled by default. Otherwise {@link DriverManager} is
 * used.
 */
public class DbConnectionManager {

  private static final String JDBC_URL =
      "jdbc:mysql://localhost:3306/" + PropertyUtils.getProperty("db.dbName")
          + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String DB_USER = PropertyUtils.getProperty("db.user");
  private static final String DB_PASSWORD = PropertyUtils.getProperty("db.password");
  private static final boolean USE_POOL = PropertyUtils.getBooleanProperty("db.usePool", true);
  private static HikariDataSource dataSource;

  static {
    try {
      Class.forName(PropertyUtils.getProperty("db.driver"));

      // Recommended defaults gathered from HikariCP documentation
      if (USE_POOL) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        dataSource = new HikariDataSource(config);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize database configuration", e);
    }
  }

  /**
   * Obtains a new connection. Consumers of these connections should close them when finished
   * (usually handled via a try-with-resources or inside {@link Transaction}).
   *
   * @return a {@code Connection} object to interact with the database
   * @throws SQLException if a database access error occurs or the connection could not be established
   */
  public static Connection getConnection() throws SQLException {
    if (USE_POOL) {
      return dataSource.getConnection();
    } else {
      return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
  }

  /**
   * Closes the HikariCP instance. Should be called when the servlet context is destroyed to
   * shut down the pool cleanly.
   */
  public static void closePool() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}